import { Injectable, NgZone } from '@angular/core';
import { DatabaseService } from '../db/app-db.service';
import { SessionApiService, SessionApiRequest } from './session-api.service';
import { ProfileApiService } from './profile-api.service';
import { firstValueFrom, fromEvent, interval } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SyncService {
  private isSyncing = false;

  constructor(
    private dbService: DatabaseService,
    private sessionApiService: SessionApiService,
    private profileApiService: ProfileApiService,
    private ngZone: NgZone
  ) {
    this.initNetworkListener();
    this.initPeriodicSync();
    // Executa a sincronização imediatamente ao carregar o aplicativo
    this.triggerSync();
  }

  /**
   * Monitora o status online do navegador para disparar sincronização ao reconectar.
   */
  private initNetworkListener() {
    this.ngZone.runOutsideAngular(() => {
      fromEvent(window, 'online').subscribe(() => {
        this.ngZone.run(() => {
          this.triggerSync();
        });
      });
    });
  }

  /**
   * Dispara um ciclo periódico de sincronização a cada 5 minutos.
   */
  private initPeriodicSync() {
    interval(300000).subscribe(() => {
      this.triggerSync();
    });
  }

  /**
   * Processo de sincronização em lote (batch sync)
   */
  async triggerSync(): Promise<void> {
    if (this.isSyncing || !navigator.onLine) {
      return;
    }
    this.isSyncing = true;

    try {
      // Buscar treinos pendentes
      const pendingSessions = await firstValueFrom(this.dbService.getPendingSessions$());

      for (const session of pendingSessions) {
        // Obter o perfil do usuário para ler o peso atualizado
        const profile = await firstValueFrom(this.dbService.getProfile(session.profileId));
        if (!profile) {
          console.error(`Perfil não encontrado para o treino ${session.clientSideUuid}. Marcando como erro.`);
          await firstValueFrom(this.dbService.updateSessionSyncStatus(session.clientSideUuid, 'ERROR'));
          continue;
        }

        // Garante que o perfil do usuário existe no backend antes de tentar sincronizar o treino
        try {
          await firstValueFrom(this.profileApiService.upsertProfile(profile));
        } catch (profileError) {
          console.warn(`Não foi possível sincronizar o perfil ${profile.id} com o backend. Pulando sincronização do treino.`, profileError);
          continue; // Pula este treino e tenta novamente no próximo ciclo
        }

        const request: SessionApiRequest = {
          clientSideUuid: session.clientSideUuid,
          profileId: session.profileId,
          type: session.type,
          durationSeconds: session.durationSeconds,
          distanceKm: session.distanceKm,
          speedKmh: session.speedKmh,
          weightKg: profile.weightKg
        };

        try {
          const response = await firstValueFrom(this.sessionApiService.postSession(request));
          // Sincronização bem-sucedida: Atualiza no banco local como SYNCED e grava ID e campos calculados do servidor
          await firstValueFrom(this.dbService.updateSessionSyncStatus(
            session.clientSideUuid, 
            'SYNCED', 
            response.id,
            {
              calculatedSpeedKmh: response.calculatedSpeedKmh,
              paceMinKm: response.paceMinKm,
              burnedCalories: response.burnedCalories,
              isStandardCircuit: response.isStandardCircuit
            }
          ));
        } catch (error: any) {
          console.error(`Falha ao sincronizar treino ${session.clientSideUuid}`, error);

          const status = error?.status;
          if (status === 400 || status === 422) {
            // Erros de validação (HTTP 400) ou inconsistência (HTTP 422) não serão retentados
            await firstValueFrom(this.dbService.updateSessionSyncStatus(session.clientSideUuid, 'ERROR'));
          } else {
            // Erros de rede/5xx: Incrementa contador. Se chegar a 5 tentativas, move para ERROR.
            await firstValueFrom(this.dbService.incrementRetryCount(session.clientSideUuid));
            const updatedSession = await firstValueFrom(this.dbService.getSession(session.clientSideUuid));
            if (updatedSession && updatedSession.retryCount >= 5) {
              await firstValueFrom(this.dbService.updateSessionSyncStatus(session.clientSideUuid, 'ERROR'));
            }
          }
        }
      }
    } catch (err) {
      console.error('Erro geral durante ciclo de sincronização:', err);
    } finally {
      this.isSyncing = false;
    }
  }
}
