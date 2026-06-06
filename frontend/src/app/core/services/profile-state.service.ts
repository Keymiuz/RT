import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { LocalProfile } from '../models/local-models';
import { DatabaseService } from '../db/app-db.service';

@Injectable({
  providedIn: 'root'
})
export class ProfileStateService {
  private activeProfileIdSubject = new BehaviorSubject<string | null>(localStorage.getItem('activeProfileId'));
  activeProfileId$: Observable<string | null> = this.activeProfileIdSubject.asObservable();

  private activeProfileSubject = new BehaviorSubject<LocalProfile | null>(null);
  activeProfile$: Observable<LocalProfile | null> = this.activeProfileSubject.asObservable();

  constructor(private dbService: DatabaseService) {
    // Sempre que o ID do perfil ativo mudar, carrega o perfil completo do IndexedDB
    this.activeProfileId$.subscribe(id => {
      if (id) {
        this.dbService.getProfile(id).subscribe(profile => {
          if (profile) {
            this.activeProfileSubject.next(profile);
          } else {
            this.activeProfileSubject.next(null);
          }
        });
      } else {
        this.activeProfileSubject.next(null);
      }
    });
  }

  /**
   * Define o perfil ativo atual e persiste sua preferência no localStorage.
   */
  setActiveProfileId(id: string | null) {
    if (id) {
      localStorage.setItem('activeProfileId', id);
    } else {
      localStorage.removeItem('activeProfileId');
    }
    this.activeProfileIdSubject.next(id);
  }

  /**
   * Obtém o perfil ativo síncronamente (snapshot atual do estado).
   */
  getActiveProfile(): LocalProfile | null {
    return this.activeProfileSubject.value;
  }
}
