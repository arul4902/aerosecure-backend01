import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-users.component.html',
  styleUrls: ['./admin-users.component.css']
})
export class AdminUsersComponent {
  username = '';
  password = '';
  confirmPassword = '';
  
  successMessage = '';
  errorMessage = '';
  isLoading = false;

  constructor(private apiService: ApiService) {}

  onSubmit(): void {
    if (!this.username.trim() || !this.password.trim()) {
      this.errorMessage = 'Username and password are required.';
      this.successMessage = '';
      return;
    }

    if (this.password !== this.confirmPassword) {
      this.errorMessage = 'Passwords do not match.';
      this.successMessage = '';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const payload = {
      username: this.username,
      password: this.password
    };

    this.apiService.createEngineer(payload).subscribe({
      next: (response) => {
        this.isLoading = false;
        if (response.success) {
          this.successMessage = `Engineer user '${response.data.username}' created successfully!`;
          this.resetForm();
        } else {
          this.errorMessage = response.message || 'Failed to create user.';
        }
      },
      error: (err) => {
        this.isLoading = false;
        if (err.error && err.error.message) {
          this.errorMessage = err.error.message;
        } else {
          this.errorMessage = 'An error occurred. Please try again.';
        }
      }
    });
  }

  private resetForm(): void {
    this.username = '';
    this.password = '';
    this.confirmPassword = '';
  }
}
