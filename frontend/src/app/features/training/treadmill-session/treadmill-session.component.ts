import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { DatabaseService } from '../../../core/db/app-db.service';
import { ProfileStateService } from '../../../core/services/profile-state.service';
import { SyncService } from '../../../core/services/sync.service';
import { LocalSession, LocalProfile } from '../../../core/models/local-models';

@Component({
  selector: 'app-treadmill-session',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './treadmill-session.component.html',
  styleUrl: './treadmill-session.component.css'
})
export class TreadmillSessionComponent implements OnInit {
  activeProfile: LocalProfile | null = null;
  formGroup!: FormGroup;
  submitted = false;

  private isUpdatingSpeed = false;

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
      durationMinutes: [null, [Validators.required, Validators.min(1), Validators.max(300)]],
      durationSeconds: [0, [Validators.required, Validators.min(0), Validators.max(59)]],
      distanceKm: [null, [Validators.required, Validators.min(0.01), Validators.max(100.0)]],
      speedKmh: [null, [Validators.required, Validators.min(1.0), Validators.max(30.0)]]
    });

    // Preenche a distância caso venha via query params (Quick-Log)
    this.route.queryParams.subscribe(params => {
      if (params['distanceKm']) {
        this.formGroup.patchValue({
          distanceKm: parseFloat(params['distanceKm'])
        });
      }
    });

    // Auto-calcula velocidade com base em tempo e distância para evitar divergências
    this.formGroup.valueChanges.subscribe(() => {
      this.updateAutoCalculatedSpeed();
    });
  }

  // ==========================================
  // Auto-cálculo da velocidade
  // ==========================================

  updateAutoCalculatedSpeed(): void {
    if (this.isUpdatingSpeed) return;

    const minutes = this.formGroup.get('durationMinutes')?.value;
    const seconds = this.formGroup.get('durationSeconds')?.value || 0;
    const distance = this.formGroup.get('distanceKm')?.value;

    if (minutes > 0 && distance > 0) {
      const totalSeconds = (minutes * 60) + seconds;
      if (totalSeconds > 0) {
        const calculatedSpeed = distance / (totalSeconds / 3600.0);
        const currentSpeed = this.formGroup.get('speedKmh')?.value;
        const speedFormatted = parseFloat(calculatedSpeed.toFixed(1));

        if (currentSpeed !== speedFormatted) {
          this.isUpdatingSpeed = true;
          this.formGroup.patchValue({
            speedKmh: speedFormatted
          }, { emitEvent: false });
          this.isUpdatingSpeed = false;
        }
      }
    }
  }

  // ==========================================
  // Métricas em Tempo Real
  // ==========================================

  get estimatedPace(): string {
    const distance = this.formGroup.get('distanceKm')?.value;
    const minutes = this.formGroup.get('durationMinutes')?.value || 0;
    const seconds = this.formGroup.get('durationSeconds')?.value || 0;
    const totalSeconds = (minutes * 60) + seconds;

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
    if (this.formGroup.invalid || !this.activeProfile) {
      return;
    }

    const formValues = this.formGroup.value;
    const totalDurationSeconds = (formValues.durationMinutes * 60) + formValues.durationSeconds;

    // Calcula os valores localmente antes de salvar
    const metrics = this.calculateMetrics(totalDurationSeconds, formValues.distanceKm, this.activeProfile.weightKg);

    const session: LocalSession = {
      clientSideUuid: crypto.randomUUID(),
      profileId: this.activeProfile.id,
      type: 'ESTEIRA',
      durationSeconds: totalDurationSeconds,
      distanceKm: formValues.distanceKm,
      speedKmh: formValues.speedKmh,
      syncStatus: 'SYNC_PENDING',
      retryCount: 0,
      createdAt: new Date().toISOString(),
      ...metrics
    };

    // Salva localmente primeiro (IndexedDB)
    this.dbService.addSession(session).subscribe(() => {
      // Dispara sincronização em background imediatamente
      this.syncService.triggerSync();
      // Retorna ao dashboard
      this.router.navigate(['/dashboard']);
    });
  }
}
