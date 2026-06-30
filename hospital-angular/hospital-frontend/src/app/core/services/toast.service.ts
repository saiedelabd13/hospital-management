import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface Toast {
  id: number;
  type: 'success' | 'error' | 'warning' | 'info';
  message: string;
  icon: string;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  private counter = 0;
  private toastsSubject = new BehaviorSubject<Toast[]>([]);
  toasts$ = this.toastsSubject.asObservable();

  private icons = {
    success: 'fa-circle-check',
    error:   'fa-circle-xmark',
    warning: 'fa-triangle-exclamation',
    info:    'fa-circle-info'
  };

  private show(type: Toast['type'], message: string, duration = 3500): void {
    const id = ++this.counter;
    const toast: Toast = { id, type, message, icon: this.icons[type] };
    this.toastsSubject.next([...this.toastsSubject.value, toast]);
    setTimeout(() => this.remove(id), duration);
  }

  success(msg: string) { this.show('success', msg); }
  error(msg: string)   { this.show('error',   msg, 5000); }
  warning(msg: string) { this.show('warning', msg); }
  info(msg: string)    { this.show('info',    msg); }

  remove(id: number): void {
    this.toastsSubject.next(this.toastsSubject.value.filter(t => t.id !== id));
  }
}
