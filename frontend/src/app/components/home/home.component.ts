import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClientModule } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';
import { ProductService } from '../../services/product.service';
import { CategoryService } from '../../services/category.service';
import { Product } from '../../models/product.model';
import { Category } from '../../models/category.model';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss'],
  providers: []  // AuthService, ProductService, and CategoryService are already provided at root
})
export class HomeComponent implements OnInit {
  username: string = '';
  firstName: string = '';
  lastName: string = '';
  products: Product[] = [];
  filteredProducts: Product[] = [];
  categories: Category[] = [];
  isLoading: boolean = false;
  errorMessage: string = '';
  successMessage: string = '';
  
  // For filtering
  searchQuery: string = '';
  sortField: string = 'name';
  sortDirection: string = 'asc';
  
  // For edit/create form
  editMode: boolean = false;
  currentProduct: Product = this.resetProductForm();
  showForm: boolean = false;
  
  // For delete confirmation dialog
  showDeleteConfirmation: boolean = false;
  productToDelete: Product | null = null;
  
  constructor(
    private router: Router,
    private authService: AuthService,
    private productService: ProductService,
    private categoryService: CategoryService
  ) {}

  ngOnInit(): void {
    // Check if user is logged in
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }
    
    // Get the current user data
    const userData = this.authService.currentUserValue;
    if (userData) {
      this.username = userData.username;  
      this.firstName = userData.firstName;  
      this.lastName = userData.lastName;
    }
    
    // Load products and categories
    this.loadProducts();
    this.loadCategories();
  }
  
  loadProducts(): void {
    this.isLoading = true;
    this.errorMessage = '';
    
    this.productService.getProducts().subscribe({
      next: (data) => {
        this.products = data;
        this.applyFiltersAndSort(); // Apply initial filtering and sorting
        this.isLoading = false;
      },
      error: (error) => {
        if (error.message === 'Your session has expired. Please login again.') {
          // This will be handled by the ProductService's error handler
          this.isLoading = false;
        } else {
          this.errorMessage = 'Failed to load products. Please try again.';
          this.isLoading = false;
          console.error('Error loading products:', error);
        }
      }
    });
  }

  loadCategories(): void {
    this.isLoading = true;
    this.errorMessage = '';
    
    this.categoryService.getCategories(true).subscribe({
      next: (data) => {
        this.categories = data;
        this.isLoading = false;
      },
      error: (error) => {
        if (error.message === 'Your session has expired. Please login again.') {
          // This will be handled by the CategoryService's error handler
          this.isLoading = false;
        } else {
          this.errorMessage = 'Failed to load categories. Please try again.';
          this.isLoading = false;
          console.error('Error loading categories:', error);
        }
      }
    });
  }
  
  addNewProduct(): void {
    this.editMode = false;
    this.currentProduct = this.resetProductForm();
    this.showForm = true;
  }
  
  editProduct(product: Product): void {
    this.editMode = true;
    this.currentProduct = { ...product };
    this.showForm = true;
  }
  
  deleteProduct(product: Product): void {
    if (!product || !product.id) return;
    
    // Show the confirmation dialog instead of using window.confirm
    this.productToDelete = product;
    this.showDeleteConfirmation = true;
  }
  
  cancelDelete(): void {
    this.showDeleteConfirmation = false;
    this.productToDelete = null;
  }
  
  confirmDelete(): void {
    if (!this.productToDelete || !this.productToDelete.id) {
      this.cancelDelete();
      return;
    }
    
    this.isLoading = true;
    this.showDeleteConfirmation = false;
    
    this.productService.deleteProduct(this.productToDelete.id).subscribe({
      next: () => {
        this.loadProducts();
        this.showSuccessMessage('Product deleted successfully');
        this.productToDelete = null;
      },
      error: (error) => {
        this.errorMessage = 'Failed to delete product. Please try again.';
        this.isLoading = false;
        this.productToDelete = null;
        console.error('Error deleting product:', error);
      }
    });
  }
  
  submitProduct(): void {
    this.isLoading = true;
    this.errorMessage = '';
    
    if (this.editMode && this.currentProduct.id) {
      // Update existing product
      this.productService.updateProduct(this.currentProduct.id, this.currentProduct).subscribe({
        next: () => {
          this.loadProducts();
          this.showSuccessMessage('Product updated successfully');
          this.cancelEdit();
        },
        error: (error) => {
          this.errorMessage = 'Failed to update product. Please try again.';
          this.isLoading = false;
          console.error('Error updating product:', error);
        }
      });
    } else {
      // Create new product
      this.productService.createProduct(this.currentProduct).subscribe({
        next: () => {
          this.loadProducts();
          this.showSuccessMessage('Product created successfully');
          this.cancelEdit();
        },
        error: (error) => {
          this.errorMessage = 'Failed to create product. Please try again.';
          this.isLoading = false;
          console.error('Error creating product:', error);
        }
      });
    }
  }
  
  cancelEdit(): void {
    this.showForm = false;
    this.currentProduct = this.resetProductForm();
  }
  
  resetProductForm(): Product {
    return {
      name: '',
      description: '',
      price: 0,
      available: true,
      category: undefined
    };
  }

  getCategoryName(product: Product): string {
    return product.category?.name || 'None';
  }
  
  compareCategories(c1: Category | undefined, c2: Category | undefined): boolean {
    return c1?.id === c2?.id;
  }
  
  navigateToCategories(): void {
    this.router.navigate(['/categories']);
  }
  
  showSuccessMessage(message: string): void {
    this.successMessage = message;
    setTimeout(() => {
      this.successMessage = '';
    }, 3000);
  }

  logout(): void {
    // Use auth service to logout
    this.authService.logout();
    // Navigate back to login page
    this.router.navigate(['/login']);
  }

  // Filtering and sorting methods
  applyFiltersAndSort(): void {
    // First filter the products
    this.filteredProducts = this.filterProducts();
    
    // Then sort the filtered products
    this.sortProducts();
  }
  
  filterProducts(): Product[] {
    if (!this.searchQuery.trim()) {
      return [...this.products]; // Return copy of all products if no search query
    }
    
    const query = this.searchQuery.toLowerCase().trim();
    return this.products.filter(product => 
      product.name.toLowerCase().includes(query) || 
      (product.description && product.description.toLowerCase().includes(query))
    );
  }
  
  sortProducts(): void {
    this.filteredProducts.sort((a, b) => {
      let valueA: any;
      let valueB: any;
      
      // Determine which field to sort by
      switch (this.sortField) {
        case 'id':
          valueA = a.id || 0;
          valueB = b.id || 0;
          break;
        case 'name':
          valueA = a.name.toLowerCase();
          valueB = b.name.toLowerCase();
          break;
        case 'description':
          valueA = (a.description || '').toLowerCase();
          valueB = (b.description || '').toLowerCase();
          break;
        case 'price':
          valueA = a.price;
          valueB = b.price;
          break;
        case 'category':
          valueA = a.category?.name?.toLowerCase() || '';
          valueB = b.category?.name?.toLowerCase() || '';
          break;
        default:
          valueA = a.name.toLowerCase();
          valueB = b.name.toLowerCase();
      }
      
      // Determine sort direction
      if (this.sortDirection === 'asc') {
        return valueA > valueB ? 1 : valueA < valueB ? -1 : 0;
      } else {
        return valueA < valueB ? 1 : valueA > valueB ? -1 : 0;
      }
    });
  }
  
  onSearch(): void {
    this.applyFiltersAndSort();
  }
  
  setSortField(field: string): void {
    // If clicking the same field, toggle direction
    if (this.sortField === field) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      // New field, set to ascending by default
      this.sortField = field;
      this.sortDirection = 'asc';
    }
    this.applyFiltersAndSort();
  }
} 