import { Routes } from '@angular/router';
import { ProfileListComponent } from './features/profiles/profile-list/profile-list.component';
import { ProfileFormComponent } from './features/profiles/profile-form/profile-form.component';
import { TrainingDashboardComponent } from './features/training/training-dashboard/training-dashboard.component';
import { StreetSessionComponent } from './features/training/street-session/street-session.component';
import { TreadmillSessionComponent } from './features/training/treadmill-session/treadmill-session.component';

export const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: TrainingDashboardComponent },
  { path: 'training/street', component: StreetSessionComponent },
  { path: 'training/treadmill', component: TreadmillSessionComponent },
  { path: 'profiles', component: ProfileListComponent },
  { path: 'profiles/new', component: ProfileFormComponent },
  { path: 'profiles/edit/:id', component: ProfileFormComponent },
  { path: '**', redirectTo: 'dashboard' }
];
