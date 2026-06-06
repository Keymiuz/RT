import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DatabaseService } from '../../../core/db/app-db.service';
import { ProfileStateService } from '../../../core/services/profile-state.service';
import { LocalProfile } from '../../../core/models/local-models';
import { Router, RouterModule } from '@angular/router';

@Component({
  selector: 'app-profile-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './profile-list.component.html',
  styleUrl: './profile-list.component.css'
})
export class ProfileListComponent implements OnInit {
  profiles: LocalProfile[] = [];
  activeProfileId: string | null = null;

  constructor(
    private dbService: DatabaseService,
    private profileState: ProfileStateService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Subscreve à lista reativa de perfis do IndexedDB
    this.dbService.getProfiles$().subscribe(list => {
      this.profiles = list;
    });

    // Subscreve ao estado do perfil ativo para destacar o selecionado
    this.profileState.activeProfileId$.subscribe(id => {
      this.activeProfileId = id;
    });
  }

  /**
   * Altera o usuário ativo globalmente no app.
   */
  selectProfile(id: string): void {
    this.profileState.setActiveProfileId(id);
    this.router.navigate(['/dashboard']);
  }

  /**
   * Redireciona para a tela de edição do perfil selecionado.
   */
  editProfile(id: string, event: Event): void {
    event.stopPropagation(); // Impede a ativação do perfil ao clicar no botão de editar
    this.router.navigate(['/profiles/edit', id]);
  }
}
