import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface SessionApiRequest {
  clientSideUuid: string;
  profileId: string;
  type: 'ESTEIRA' | 'RUA';
  durationSeconds: number;
  distanceKm: number;
  speedKmh?: number;
  weightKg: number;
}

export interface SessionApiResponse {
  id: string;
  clientSideUuid: string;
  profileId: string;
  type: 'ESTEIRA' | 'RUA';
  durationSeconds: number;
  distanceKm: number;
  speedKmh?: number;
  calculatedSpeedKmh: number;
  paceMinKm: number;
  burnedCalories: number;
  isStandardCircuit: boolean;
  createdAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class SessionApiService {
  private baseUrl = 'http://localhost:8080/api/sessions';

  constructor(private http: HttpClient) {}

  /**
   * Envia o treino para processamento e persistência no Spring Boot.
   * Injeta obrigatoriamente o cabeçalho X-Client-UUID para idempotência.
   */
  postSession(session: SessionApiRequest): Observable<SessionApiResponse> {
    const headers = new HttpHeaders({
      'X-Client-UUID': session.clientSideUuid
    });

    return this.http.post<SessionApiResponse>(this.baseUrl, session, { headers });
  }

  /**
   * Obtém a lista de treinos de um perfil específico do backend.
   */
  getSessions(profileId: string): Observable<SessionApiResponse[]> {
    return this.http.get<SessionApiResponse[]>(`${this.baseUrl}?profileId=${profileId}`);
  }
}
