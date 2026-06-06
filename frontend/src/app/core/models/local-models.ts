export interface LocalProfile {
  id: string; // UUID usado como chave primária
  name: string;
  weightKg: number;
  targetPace: number; // em decimal min/km
  updatedAt: string; // Data ISO da última edição (Last Write Wins)
}

export interface LocalSession {
  clientSideUuid: string; // UUID gerado no cliente (Chave Primária)
  id?: string; // UUID gerado no backend após a sincronização
  profileId: string; // Vínculo com o perfil do usuário
  type: 'ESTEIRA' | 'RUA';
  durationSeconds: number;
  distanceKm: number;
  speedKmh?: number; // Preenchido apenas no modo ESTEIRA
  calculatedSpeedKmh?: number; // Computado no backend
  paceMinKm?: number; // Computado no backend
  burnedCalories?: number; // Computado no backend
  isStandardCircuit?: boolean; // Identificação de 5.5km
  syncStatus: 'SYNCED' | 'SYNC_PENDING' | 'ERROR'; // Status de sincronização
  retryCount: number; // Contador de tentativas para o background sync
  createdAt: string; // Data ISO de criação da atividade
}
