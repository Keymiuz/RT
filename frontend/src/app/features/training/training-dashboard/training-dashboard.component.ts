import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DatabaseService } from '../../../core/db/app-db.service';
import { ProfileStateService } from '../../../core/services/profile-state.service';
import { LocalProfile, LocalSession } from '../../../core/models/local-models';
import { Router, RouterModule } from '@angular/router';
import { Observable, of } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';
import { ProfileSwitcherComponent } from '../../profiles/profile-switcher/profile-switcher.component';

@Component({
  selector: 'app-training-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, ProfileSwitcherComponent],
  templateUrl: './training-dashboard.component.html',
  styleUrl: './training-dashboard.component.css'
})
export class TrainingDashboardComponent implements OnInit {
  activeProfile$: Observable<LocalProfile | null>;
  sessions$: Observable<LocalSession[]>;
  recentSessions$: Observable<LocalSession[]>;
  chartSessions$: Observable<LocalSession[]>;
  totalDistance$: Observable<number>;
  averagePace$: Observable<number>;

  constructor(
    private dbService: DatabaseService,
    private profileState: ProfileStateService,
    private router: Router
  ) {
    this.activeProfile$ = this.profileState.activeProfile$;

    // Carrega sessões ordenadas por data decrescente
    this.sessions$ = this.activeProfile$.pipe(
      switchMap(profile => {
        if (!profile) return of([]);
        return this.dbService.getSessionsByProfile$(profile.id);
      }),
      map(sessions => sessions.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()))
    );

    // Distância total acumulada de todas as sessões
    this.totalDistance$ = this.sessions$.pipe(
      map(sessions => sessions.reduce((acc, s) => acc + (s.distanceKm || 0), 0))
    );

    // Últimos 5 treinos para a listagem
    this.recentSessions$ = this.sessions$.pipe(
      map(sessions => sessions.slice(0, 5))
    );

    // Treinos para o gráfico de evolução (até 10 treinos com ritmo válidos, ordenados do mais antigo ao mais recente)
    this.chartSessions$ = this.sessions$.pipe(
      map(sessions => {
        const withPace = sessions.filter(s => s.paceMinKm !== undefined && s.paceMinKm > 0);
        return withPace.slice(0, 10).reverse();
      })
    );

    // Calcula o ritmo médio geral de todas as sessões
    this.averagePace$ = this.sessions$.pipe(
      map(sessions => {
        const sessionsWithPace = sessions.filter(s => s.paceMinKm !== undefined && s.paceMinKm > 0);
        if (sessionsWithPace.length === 0) return 0;
        const sum = sessionsWithPace.reduce((acc, s) => acc + (s.paceMinKm || 0), 0);
        return sum / sessionsWithPace.length;
      })
    );
  }

  ngOnInit(): void {
    // Se não houver perfil ativo, redireciona para a tela de perfis
    this.profileState.activeProfileId$.subscribe(id => {
      if (!id) {
        this.router.navigate(['/profiles']);
      }
    });
  }

  /**
   * Remove um treino localmente (IndexedDB)
   */
  deleteSession(uuid: string, event: Event): void {
    event.stopPropagation();
    if (confirm('Deseja realmente excluir este treino?')) {
      this.dbService.deleteSession(uuid).subscribe();
    }
  }

  /**
   * Converte pace decimal para formato mm:ss (ex: 5.45 -> 5:27)
   */
  formatPace(pace?: number): string {
    if (!pace) return '0:00';
    const mins = Math.floor(pace);
    const secs = Math.round((pace - mins) * 60);
    return `${mins}:${secs < 10 ? '0' : ''}${secs}`;
  }

  /**
   * Converte velocidade para km/h formatado
   */
  formatSpeed(speed?: number): string {
    return speed ? speed.toFixed(1) : '0.0';
  }

  /**
   * Converte duração em segundos para mm:ss ou hh:mm:ss
   */
  formatDuration(seconds: number): string {
    const hrs = Math.floor(seconds / 3600);
    const mins = Math.floor((seconds - (hrs * 3600)) / 60);
    const secs = seconds % 60;
    if (hrs > 0) {
      return `${hrs}:${mins < 10 ? '0' : ''}${mins}:${secs < 10 ? '0' : ''}${secs}`;
    }
    return `${mins}:${secs < 10 ? '0' : ''}${secs}`;
  }

  /**
   * Retorna a altura em porcentagem da barra no gráfico de evolução.
   * Altura proporcional à velocidade (menor pace = mais rápido = barra mais alta).
   */
  getPacePercentage(pace?: number): number {
    if (!pace) return 0;
    const maxPace = 10; // Pace de 10 min/km (lento, 10% altura)
    const minPace = 3;  // Pace de 3 min/km (rápido, 100% altura)
    if (pace > maxPace) return 10;
    if (pace < minPace) return 100;
    return ((maxPace - pace) / (maxPace - minPace)) * 80 + 20;
  }

  /**
   * Retorna a classe CSS correspondente ao status de sincronização
   */
  getSyncBadgeClass(status?: string): string {
    if (status === 'SYNCED') return 'badge-synced';
    if (status === 'SYNC_PENDING') return 'badge-sync_pending';
    return 'badge-error';
  }
}
