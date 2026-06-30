import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: 'auth',
    loadChildren: () => import('./features/auth/auth.routes').then(m => m.AUTH_ROUTES)
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./layout/shell/shell.component').then(m => m.ShellComponent),
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      {
        path: 'dashboard',
        loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent),
        title: 'لوحة التحكم'
      },
      {
        path: 'departments',
        loadComponent: () => import('./features/departments/departments.component').then(m => m.DepartmentsComponent),
        title: 'الأقسام'
      },
      {
        path: 'doctors',
        loadComponent: () => import('./features/doctors/doctors.component').then(m => m.DoctorsComponent),
        title: 'الأطباء'
      },
      {
        path: 'patients',
        loadComponent: () => import('./features/patients/patients.component').then(m => m.PatientsComponent),
        title: 'المرضى'
      },
      {
        path: 'appointments',
        loadComponent: () => import('./features/appointments/appointments.component').then(m => m.AppointmentsComponent),
        title: 'المواعيد'
      },
      {
        path: 'medical-records',
        loadComponent: () => import('./features/medical-records/medical-records.component').then(m => m.MedicalRecordsComponent),
        title: 'السجلات الطبية'
      }
    ]
  },
  { path: '**', redirectTo: '' }
];
