import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Product } from '../models/product.model';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private apiUrl = `${environment.apiUrl}/api/products`;

  constructor(
    private http: HttpClient, 
    private router: Router,
    private authService: AuthService
  ) { }

  // Get all products
  getProducts(): Observable<Product[]> {
    return this.http.get<Product[]>(this.apiUrl)
      .pipe(
        catchError(error => this.handleError(error))
      );
  }

  // Get a single product by ID
  getProduct(id: number): Observable<Product> {
    return this.http.get<Product>(`${this.apiUrl}/${id}`)
      .pipe(
        catchError(error => this.handleError(error))
      );
  }

  // Create a new product
  createProduct(product: Product): Observable<Product> {
    return this.http.post<Product>(this.apiUrl, product)
      .pipe(
        catchError(error => this.handleError(error))
      );
  }

  // Update an existing product
  updateProduct(id: number, product: Product): Observable<Product> {
    return this.http.put<Product>(`${this.apiUrl}/${id}`, product)
      .pipe(
        catchError(error => this.handleError(error))
      );
  }

  // Delete a product
  deleteProduct(id: number): Observable<void> {
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