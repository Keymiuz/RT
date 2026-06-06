import { Component, OnInit, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { DatabaseService } from '../../../core/db/app-db.service';
import { ProfileApiService } from '../../../core/services/profile-api.service';
import { ProfileStateService } from '../../../core/services/profile-state.service';
import { LocalProfile } from '../../../core/models/local-models';
import { Router, RouterModule } from '@angular/router';

@Component({
  selector: 'app-profile-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './profile-form.component.html',
  styleUrl: './profile-form.component.css'
})
export class ProfileFormComponent implements OnInit {
  @Input() id?: string; // ID recebido via parâmetros de rota para modo edição
  profileForm!: FormGroup;
  isEditMode = false;
  submitted = false;

  constructor(
    private fb: FormBuilder,
    private dbService: DatabaseService,
    private profileApiService: ProfileApiService,
    private profileState: ProfileStateService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.profileForm = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(50)]],
      weightKg: [null, [Validators.required, Validators.min(10), Validators.max(250)]],
      targetPace: [null, [Validators.required, Validators.min(1.0), Validators.max(30.0)]]
    });

    if (this.id) {
      this.isEditMode = true;
      this.dbService.getProfile(this.id).subscribe(profile => {
        if (profile) {
          this.profileForm.patchValue({
            name: profile.name,
            weightKg: profile.weightKg,
            targetPace: profile.targetPace
          });
        }
      });
    }
  }

  onSubmit(): void {
    this.submitted = true;
    if (this.profileForm.invalid) {
      return;
    }

    const formValues = this.profileForm.value;
    const profile: LocalProfile = {
      id: this.id || crypto.randomUUID(),
      name: formValues.name,
      weightKg: formValues.weightKg,
      targetPace: formValues.targetPace,
      updatedAt: new Date().toISOString()
    };

    // 1. Salva localmente primeiro (IndexedDB)
    this.dbService.upsertProfile(profile).subscribe(() => {
      // Se não houver perfil ativo ou se estivermos editando o perfil ativo atual, atualiza o estado
      const active = this.profileState.getActiveProfile();
      if (!active || active.id === profile.id) {
        this.profileState.setActiveProfileId(profile.id);
      }

      // 2. Tenta sincronizar com o backend em background
      this.profileApiService.upsertProfile(profile).subscribe({
        next: () => console.log('Perfil sincronizado com o servidor.'),
        error: (err) => console.warn('Falha ao sincronizar perfil. Sincronização ocorrerá ao recuperar rede.', err)
      });

      // Redireciona de volta para a listagem
      this.router.navigate(['/profiles']);
    });
  }

  cancel(): void {
    this.router.navigate(['/profiles']);
  }
}
