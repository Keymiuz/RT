import { Injectable } from '@angular/core';
import Dexie, { Table } from 'dexie';
import { from, Observable } from 'rxjs';
import { liveQuery } from 'dexie';
import { LocalProfile, LocalSession } from '../models/local-models';

/**
 * Instância local do Banco de Dados no navegador via Dexie.js
 */
export class AppDatabase extends Dexie {
  profiles!: Table<LocalProfile, string>;
  sessions!: Table<LocalSession, string>;

  constructor() {
    super('PrecisionTrackerDB');
    this.version(1).stores({
      profiles: 'id, name, updatedAt',
      sessions: 'clientSideUuid, profileId, syncStatus'
    });
  }
}

/**
 * Serviço reativo Angular para encapsular o acesso ao banco local IndexedDB
 */
@Injectable({
  providedIn: 'root'
})
export class DatabaseService {
  private db = new AppDatabase();

  constructor() {}

  // ==========================================
  // Operações de Perfil (Profiles)
  // ==========================================

  /**
   * Adiciona ou atualiza um perfil no IndexedDB.
   */
  upsertProfile(profile: LocalProfile): Observable<string> {
    return from(this.db.profiles.put(profile));
  }

  /**
   * Obtém um perfil específico pelo seu ID (UUID).
   */
  getProfile(id: string): Observable<LocalProfile | undefined> {
    return from(this.db.profiles.get(id));
  }

  /**
   * Retorna um Observable reativo com todos os perfis cadastrados.
   */
  getProfiles$(): Observable<LocalProfile[]> {
    return from(liveQuery(() => this.db.profiles.toArray()));
  }

  // ==========================================
  // Operações de Sessão de Corrida (Sessions)
  // ==========================================

  /**
   * Salva ou atualiza um treino localmente (Write-to-Local-First).
   */
  addSession(session: LocalSession): Observable<string> {
    return from(this.db.sessions.put(session));
  }

  /**
   * Obtém um treino específico a partir do clientSideUuid.
   */
  getSession(clientSideUuid: string): Observable<LocalSession | undefined> {
    return from(this.db.sessions.get(clientSideUuid));
  }

  /**
   * Retorna um Observable reativo contendo o histórico de treinos de um perfil específico.
   */
  getSessionsByProfile$(profileId: string): Observable<LocalSession[]> {
    return from(liveQuery(() => 
      this.db.sessions.where('profileId').equals(profileId).toArray()
    ));
  }

  /**
   * Retorna um Observable reativo com todas as sessões que aguardam sincronização (syncStatus = 'SYNC_PENDING').
   */
  getPendingSessions$(): Observable<LocalSession[]> {
    return from(liveQuery(() => 
      this.db.sessions.where('syncStatus').equals('SYNC_PENDING').toArray()
    ));
  }

  updateSessionSyncStatus(
    clientSideUuid: string, 
    status: 'SYNCED' | 'SYNC_PENDING' | 'ERROR', 
    serverId?: string, 
    calculatedFields?: Partial<LocalSession>
  ): Observable<number> {
    const updateData: Partial<LocalSession> = { syncStatus: status, ...calculatedFields };
    if (serverId) {
      updateData.id = serverId;
    }
    return from(this.db.sessions.update(clientSideUuid, updateData));
  }

  /**
   * Incrementa o contador de retentativas de sincronização de um treino que falhou.
   */
  incrementRetryCount(clientSideUuid: string): Observable<number> {
    return from(this.db.sessions.where('clientSideUuid').equals(clientSideUuid).modify(session => {
      session.retryCount = (session.retryCount || 0) + 1;
    }));
  }

  /**
   * Remove uma sessão local do IndexedDB.
   */
  deleteSession(clientSideUuid: string): Observable<void> {
    return from(this.db.sessions.delete(clientSideUuid));
  }
}
