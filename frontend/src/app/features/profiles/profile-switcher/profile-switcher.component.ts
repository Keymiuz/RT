import { Component, OnInit, HostListener, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DatabaseService } from '../../../core/db/app-db.service';
import { ProfileStateService } from '../../../core/services/profile-state.service';
import { LocalProfile } from '../../../core/models/local-models';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-profile-switcher',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './profile-switcher.component.html',
  styleUrl: './profile-switcher.component.css'
})
export class ProfileSwitcherComponent implements OnInit {
  profiles: LocalProfile[] = [];
  activeProfile: LocalProfile | null = null;
  isOpen = false;



  constructor(
    private dbService: DatabaseService,
    private profileState: ProfileStateService,
    private elementRef: ElementRef
  ) {}

  ngOnInit(): void {
    // Subscreve aos perfis do IndexedDB
    this.dbService.getProfiles$().subscribe(list => {
      this.profiles = list;
    });

    // Subscreve ao perfil ativo
    this.profileState.activeProfile$.subscribe(profile => {
      this.activeProfile = profile;
    });
  }

  toggleDropdown(): void {
    this.isOpen = !this.isOpen;
  }

  selectProfile(id: string): void {
    this.profileState.setActiveProfileId(id);
    this.isOpen = false;
  }

  // Fecha o dropdown caso o usuário clique fora do componente
  @HostListener('document:click', ['$event'])
  onClickOutside(event: Event): void {
    if (!this.elementRef.nativeElement.contains(event.target)) {
      this.isOpen = false;
    }
  }


}
