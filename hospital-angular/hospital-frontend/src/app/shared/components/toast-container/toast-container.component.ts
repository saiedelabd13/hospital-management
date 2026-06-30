import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-toast-container',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="toast-container">
      <div *ngFor="let t of toast.toasts$ | async"
           class="toast toast-{{t.type}}"
           (click)="toast.remove(t.id)">
        <i class="fas {{t.icon}}"></i>
        <span>{{t.message}}</span>
      </div>
    </div>
  `
})
export class ToastContainerComponent {
  constructor(public toast: ToastService) {}
}
