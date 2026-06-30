import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '@env/environment';
import { JwtResponse, LoginRequest } from '@models/index';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly API = `${environment.apiUrl}/auth`;
  private readonly TOKEN_KEY = 'hospital_token';
  private readonly USER_KEY  = 'hospital_user';

  private currentUserSubject = new BehaviorSubject<JwtResponse | null>(this.storedUser);
  currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient, private router: Router) {}

  get storedUser(): JwtResponse | null {
    const raw = localStorage.getItem(this.USER_KEY);
    return raw ? JSON.parse(raw) : null;
  }

  get currentUser(): JwtResponse | null {
    return this.currentUserSubject.value;
  }

  get token(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  get isLoggedIn(): boolean {
    return !!this.token;
  }

  hasRole(role: string): boolean {
    return this.currentUser?.roles.includes(role) ?? false;
  }

  isAdmin(): boolean       { return this.hasRole('ROLE_ADMIN'); }
  isDoctor(): boolean      { return this.hasRole('ROLE_DOCTOR'); }
  isReceptionist(): boolean { return this.hasRole('ROLE_RECEPTIONIST'); }

  login(req: LoginRequest): Observable<JwtResponse> {
    return this.http.post<JwtResponse>(`${this.API}/login`, req).pipe(
      tap(res => {
        localStorage.setItem(this.TOKEN_KEY, res.token);
        localStorage.setItem(this.USER_KEY, JSON.stringify(res));
        this.currentUserSubject.next(res);
      })
    );
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.currentUserSubject.next(null);
    this.router.navigate(['/auth/login']);
  }
}
