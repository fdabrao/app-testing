import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { CategoryService } from '../../services/category.service';
import { Category } from '../../models/category.model';

@Component({
  selector: 'app-category',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './category.component.html',
  styleUrls: ['./category.component.scss']
})
export class CategoryComponent implements OnInit {
  username: string = '';
  firstName: string = '';
  lastName: string = '';
  categories: Category[] = [];
  parentCategories: Category[] = [];
  isLoading: boolean = false;
  errorMessage: string = '';
  successMessage: string = '';
  
  // For edit/create form
  editMode: boolean = false;
  currentCategory: Category = this.resetCategoryForm();
  showForm: boolean = false;
  
  // For delete confirmation dialog
  showDeleteConfirmation: boolean = false;
  categoryToDelete: Category | null = null;
  
  constructor(
    private router: Router,
    private authService: AuthService,
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
    
    // Load categories
    this.loadCategories();
  }
  
  loadCategories(): void {
    this.isLoading = true;
    this.errorMessage = '';
    
    this.categoryService.getCategories().subscribe({
      next: (data) => {
        this.categories = data;
        this.parentCategories = [...data];
        this.isLoading = false;
      },
      error: (error) => {
        if (error.message === 'Your session has expired. Please login again.') {
          this.isLoading = false;
        } else {
          this.errorMessage = 'Failed to load categories. Please try again.';
          this.isLoading = false;
          console.error('Error loading categories:', error);
        }
      }
    });
  }
  
  addNewCategory(): void {
    this.editMode = false;
    this.currentCategory = this.resetCategoryForm();
    this.showForm = true;
  }
  
  editCategory(category: Category): void {
    this.editMode = true;
    this.currentCategory = { ...category };
    this.showForm = true;
  }
  
  deleteCategory(category: Category): void {
    if (!category || !category.id) return;
    
    // Show the confirmation dialog
    this.categoryToDelete = category;
    this.showDeleteConfirmation = true;
  }
  
  cancelDelete(): void {
    this.showDeleteConfirmation = false;
    this.categoryToDelete = null;
  }
  
  confirmDelete(): void {
    if (!this.categoryToDelete || !this.categoryToDelete.id) {
      this.cancelDelete();
      return;
    }
    
    this.isLoading = true;
    this.showDeleteConfirmation = false;
    
    this.categoryService.deleteCategory(this.categoryToDelete.id).subscribe({
      next: () => {
        this.loadCategories();
        this.showSuccessMessage('Category deleted successfully');
        this.categoryToDelete = null;
      },
      error: (error) => {
        this.errorMessage = 'Failed to delete category. Please try again.';
        this.isLoading = false;
        this.categoryToDelete = null;
        console.error('Error deleting category:', error);
      }
    });
  }
  
  submitCategory(): void {
    this.isLoading = true;
    this.errorMessage = '';
    
    if (this.editMode && this.currentCategory.id) {
      // Update existing category
      this.categoryService.updateCategory(this.currentCategory.id, this.currentCategory).subscribe({
        next: () => {
          this.loadCategories();
          this.showSuccessMessage('Category updated successfully');
          this.cancelEdit();
        },
        error: (error) => {
          this.errorMessage = 'Failed to update category. Please try again.';
          this.isLoading = false;
          console.error('Error updating category:', error);
        }
      });
    } else {
      // Create new category
      this.categoryService.createCategory(this.currentCategory).subscribe({
        next: () => {
          this.loadCategories();
          this.showSuccessMessage('Category created successfully');
          this.cancelEdit();
        },
        error: (error) => {
          this.errorMessage = 'Failed to create category. Please try again.';
          this.isLoading = false;
          console.error('Error creating category:', error);
        }
      });
    }
  }
  
  cancelEdit(): void {
    this.showForm = false;
    this.currentCategory = this.resetCategoryForm();
  }
  
  resetCategoryForm(): Category {
    return {
      name: '',
      description: '',
      parentCategory: undefined,
      active: true
    };
  }
  
  compareCategories(c1: Category | undefined, c2: Category | undefined): boolean {
    return c1?.id === c2?.id;
  }
  
  navigateToProducts(): void {
    this.router.navigate(['/home']);
  }
  
  showSuccessMessage(message: string): void {
    this.successMessage = message;
    setTimeout(() => {
      this.successMessage = '';
    }, 3000);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
} 