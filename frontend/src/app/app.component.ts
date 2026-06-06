import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { SyncService } from './core/services/sync.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'frontend';

  constructor(private syncService: SyncService) {}
}
