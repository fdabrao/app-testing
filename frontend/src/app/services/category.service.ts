import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Category } from '../models/category.model';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class CategoryService {
  private apiUrl = `${environment.apiUrl}/api/categories`;

  constructor(
    private http: HttpClient,
    private router: Router,
    private authService: AuthService
  ) { }

  // Get all categories
  getCategories(activeOnly: boolean = false): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.apiUrl}${activeOnly ? '?activeOnly=true' : ''}`)
      .pipe(
        catchError(error => this.handleError(error))
      );
  }

  // Get a single category by ID
  getCategory(id: number): Observable<Category> {
    return this.http.get<Category>(`${this.apiUrl}/${id}`)
      .pipe(
        catchError(error => this.handleError(error))
      );
  }

  // Get categories by parent category
  getCategoriesByParent(parentCategory: string): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.apiUrl}/byParent/${parentCategory}`)
      .pipe(
        catchError(error => this.handleError(error))
      );
  }

  // Create a new category
  createCategory(category: Category): Observable<Category> {
    return this.http.post<Category>(this.apiUrl, category)
      .pipe(
        catchError(error => this.handleError(error))
      );
  }

  // Update an existing category
  updateCategory(id: number, category: Category): Observable<Category> {
    return this.http.put<Category>(`${this.apiUrl}/${id}`, category)
      .pipe(
        catchError(error => this.handleError(error))
      );
  }

  // Delete a category
  deleteCategory(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`)
      .pipe(
        catchError(error => this.handleError(error))
      );
  }

  // Error handling
  private handleError(error: HttpErrorResponse) {
    if (error.status === 401) {
      // If 401 Unauthorized, the token is likely expired or invalid
      console.error('Authentication error:', error);
      this.authService.logout();
      this.router.navigate(['/login']);
      return throwError(() => new Error('Your session has expired. Please login again.'));
    }
    
    // Return a user-friendly error message
    const errorMessage = error.error?.message || 'An error occurred. Please try again.';
    console.error('API error:', error);
    return throwError(() => new Error(errorMessage));
  }
} 