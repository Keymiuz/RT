import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { DatabaseService } from '../../../core/db/app-db.service';
import { ProfileStateService } from '../../../core/services/profile-state.service';
import { SyncService } from '../../../core/services/sync.service';
import { LocalSession, LocalProfile } from '../../../core/models/local-models';
import { Subscription, timer } from 'rxjs';

@Component({
  selector: 'app-street-session',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './street-session.component.html',
  styleUrl: './street-session.component.css'
})
export class StreetSessionComponent implements OnInit, OnDestroy {
  activeProfile: LocalProfile | null = null;
  formGroup!: FormGroup;
  submitted = false;

  // Estados do Cronômetro e Modo Manual
  elapsedSeconds = 0;
  isRunning = false;
  timerSub?: Subscription;
  isManualMode = false;

  private startTime = 0;
  private accumulatedTimeMs = 0;

  constructor(
    private fb: FormBuilder,
    private dbService: DatabaseService,
    private profileState: ProfileStateService,
    private syncService: SyncService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Escuta o perfil ativo
    this.profileState.activeProfile$.subscribe(profile => {
      this.activeProfile = profile;
      if (!profile) {
        this.router.navigate(['/profiles']);
      }
    });

    this.formGroup = this.fb.group({
      distanceKm: [null, [Validators.required, Validators.min(0.01), Validators.max(100.0)]],
      durationMinutes: [null, [Validators.min(0), Validators.max(300)]],
      durationSeconds: [0, [Validators.min(0), Validators.max(59)]]
    });

    // Lê query params para verificar se o Quick-Log de 5.5km foi ativado
    this.route.queryParams.subscribe(params => {
      if (params['distanceKm']) {
        this.formGroup.patchValue({
          distanceKm: parseFloat(params['distanceKm'])
        });
      }
    });
  }

  ngOnDestroy(): void {
    this.stopStopwatch();
  }

  // ==========================================
  // Alternância de Modo
  // ==========================================

  toggleManualMode(): void {
    this.isManualMode = !this.isManualMode;
    this.submitted = false;
    if (this.isManualMode) {
      this.stopStopwatch();
      this.formGroup.get('durationMinutes')?.setValidators([Validators.required, Validators.min(0), Validators.max(300)]);
      this.formGroup.get('durationSeconds')?.setValidators([Validators.required, Validators.min(0), Validators.max(59)]);
    } else {
      this.formGroup.get('durationMinutes')?.clearValidators();
      this.formGroup.get('durationSeconds')?.clearValidators();
    }
    this.formGroup.get('durationMinutes')?.updateValueAndValidity();
    this.formGroup.get('durationSeconds')?.updateValueAndValidity();
  }

  // ==========================================
  // Controle do Cronômetro (Stopwatch)
  // ==========================================

  startStopwatch(): void {
    if (this.isRunning) return;
    this.isRunning = true;
    this.startTime = Date.now();
    // Utiliza timer(0, 1000) do RxJS para disparar atualizações e delta de timestamp para precisão milimétrica
    this.timerSub = timer(0, 1000).subscribe(() => {
      const currentMs = this.accumulatedTimeMs + (Date.now() - this.startTime);
      this.elapsedSeconds = Math.floor(currentMs / 1000);
    });
  }

  pauseStopwatch(): void {
    if (!this.isRunning) return;
    this.accumulatedTimeMs += Date.now() - this.startTime;
    this.elapsedSeconds = Math.floor(this.accumulatedTimeMs / 1000);
    this.stopStopwatch();
  }

  resetStopwatch(): void {
    this.stopStopwatch();
    this.accumulatedTimeMs = 0;
    this.elapsedSeconds = 0;
  }

  private stopStopwatch(): void {
    this.isRunning = false;
    if (this.timerSub) {
      this.timerSub.unsubscribe();
      this.timerSub = undefined;
    }
  }

  formatTime(seconds: number): string {
    const hrs = Math.floor(seconds / 3600);
    const mins = Math.floor((seconds - (hrs * 3600)) / 60);
    const secs = seconds % 60;
    return `${hrs < 10 ? '0' : ''}${hrs}:${mins < 10 ? '0' : ''}${mins}:${secs < 10 ? '0' : ''}${secs}`;
  }

  // ==========================================
  // Métricas em Tempo Real
  // ==========================================

  get estimatedPace(): string {
    const distance = this.formGroup.get('distanceKm')?.value;
    const minutes = this.formGroup.get('durationMinutes')?.value || 0;
    const seconds = this.formGroup.get('durationSeconds')?.value || 0;
    const totalSeconds = this.isManualMode ? (minutes * 60) + seconds : this.elapsedSeconds;

    if (!distance || totalSeconds <= 0) return '0:00';
    const pace = (totalSeconds / 60.0) / distance;
    return this.formatPace(pace);
  }

  formatPace(pace: number): string {
    const mins = Math.floor(pace);
    const secs = Math.round((pace - mins) * 60);
    return `${mins}:${secs < 10 ? '0' : ''}${secs}`;
  }

  /**
   * Executa cálculos ACSM MET idênticos aos do backend localmente
   */
  calculateMetrics(durationSeconds: number, distanceKm: number, weightKg: number) {
    const calculatedSpeedKmh = distanceKm / (durationSeconds / 3600.0);
    const paceMinKm = (durationSeconds / 60.0) / distanceKm;
    const speed_m_min = calculatedSpeedKmh * 16.6667;
    const MET = 3.5 + (0.2 * speed_m_min);
    const burnedCalories = (MET * 3.5 * weightKg / 200.0) * (durationSeconds / 60.0);
    const isStandardCircuit = Math.abs(distanceKm - 5.5) < 0.01; // circuit padrão 5.5km
    return { paceMinKm, calculatedSpeedKmh, burnedCalories, isStandardCircuit };
  }

  // ==========================================
  // Persistência
  // ==========================================

  saveSession(): void {
    this.submitted = true;
    if (!this.activeProfile) {
      return;
    }

    const distance = this.formGroup.value.distanceKm;
    let durationSeconds = 0;

    if (this.isManualMode) {
      if (this.formGroup.invalid) return;
      const minutes = this.formGroup.value.durationMinutes || 0;
      const seconds = this.formGroup.value.durationSeconds || 0;
      durationSeconds = (minutes * 60) + seconds;
    } else {
      if (this.formGroup.get('distanceKm')?.invalid || this.elapsedSeconds <= 0) return;
      durationSeconds = this.elapsedSeconds;
    }

    if (durationSeconds <= 0 || !distance) {
      return;
    }

    // Calcula os valores localmente antes de salvar
    const metrics = this.calculateMetrics(durationSeconds, distance, this.activeProfile.weightKg);

    const session: LocalSession = {
      clientSideUuid: crypto.randomUUID(),
      profileId: this.activeProfile.id,
      type: 'RUA',
      durationSeconds: durationSeconds,
      distanceKm: distance,
      syncStatus: 'SYNC_PENDING',
      retryCount: 0,
      createdAt: new Date().toISOString(),
      ...metrics
    };

    // Salva localmente primeiro (IndexedDB)
    this.dbService.addSession(session).subscribe(() => {
      // Força a execução imediata da sincronização com o Spring Boot
      this.syncService.triggerSync();
      // Retorna ao dashboard principal
      this.router.navigate(['/dashboard']);
    });
  }
}
