import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, BehaviorSubject, throwError } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { Router } from '@angular/router';
import { environment } from '../../environments/environment';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly JWT_TOKEN = 'JWT_TOKEN';
  private readonly USER_DATA = 'USER_DATA';
  private currentUserSubject: BehaviorSubject<LoginResponse | null>;
  private apiUrl = `${environment.apiUrl}/api/auth`;
  
  constructor(
    private http: HttpClient,
    private router: Router
  ) {
    this.currentUserSubject = new BehaviorSubject<LoginResponse | null>(this.getUserFromLocalStorage());
  }
  
  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, credentials)
      .pipe(
        tap(response => {
          // Store auth details and jwt in local storage
          this.setSession(response);
          this.currentUserSubject.next(response);
        }),
        catchError(error => this.handleError(error))
      );
  }
  
  logout(): void {
    // Remove user from local storage and set current user to null
    localStorage.removeItem(this.JWT_TOKEN);
    localStorage.removeItem(this.USER_DATA);
    this.currentUserSubject.next(null);
    
    // Navigate to login page
    this.router.navigate(['/login']);
  }
  
  private setSession(authResult: LoginResponse): void {
    localStorage.setItem(this.JWT_TOKEN, authResult.token);
    localStorage.setItem(this.USER_DATA, JSON.stringify(authResult));
  }
  
  getToken(): string | null {
    return localStorage.getItem(this.JWT_TOKEN);
  }
  
  getUserFromLocalStorage(): LoginResponse | null {
    const userData = localStorage.getItem(this.USER_DATA);
    return userData ? JSON.parse(userData) : null;
  }
  
  get currentUser(): Observable<LoginResponse | null> {
    return this.currentUserSubject.asObservable();
  }
  
  get currentUserValue(): LoginResponse | null {
    return this.currentUserSubject.value;
  }
  
  isLoggedIn(): boolean {
    return !!this.getToken();
  }
  
  // Error handling
  private handleError(error: HttpErrorResponse) {
    let errorMessage = 'An error occurred. Please try again.';
    
    if (error.error instanceof ErrorEvent) {
      // Client-side error
      errorMessage = `Error: ${error.error.message}`;
    } else {
      // Server-side error
      if (error.status === 401) {
        errorMessage = 'Invalid username or password';
      } else if (error.error && error.error.message) {
        errorMessage = error.error.message;
      } else {
        errorMessage = `Error Code: ${error.status}, Message: ${error.message}`;
      }
    }
    
    console.error('Auth service error:', error);
    return throwError(() => new Error(errorMessage));
  }
} 