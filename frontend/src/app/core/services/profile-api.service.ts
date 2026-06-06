import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { LocalProfile } from '../models/local-models';

@Injectable({
  providedIn: 'root'
})
export class ProfileApiService {
  private baseUrl = 'http://localhost:8080/api/profiles';

  constructor(private http: HttpClient) {}

  /**
   * Envia o perfil do usuário para salvar/sincronizar no backend (LWW).
   */
  upsertProfile(profile: LocalProfile): Observable<LocalProfile> {
    return this.http.post<LocalProfile>(this.baseUrl, profile);
  }

  /**
   * Obtém o perfil completo do backend pelo ID (UUID).
   */
  getProfile(id: string): Observable<LocalProfile> {
    return this.http.get<LocalProfile>(`${this.baseUrl}/${id}`);
  }
}
