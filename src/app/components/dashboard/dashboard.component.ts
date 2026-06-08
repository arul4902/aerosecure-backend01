import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  aircraftList: any[] = [];
  
  // Pagination & sorting
  page = 0;
  size = 5;
  sortBy = 'aircraftId';
  totalPages = 0;
  totalElements = 0;
  
  // Filtering & Search
  searchManufacturer = '';
  filterStatus = 'ALL';
  
  // Form State
  isFormOpen = false;
  isEditMode = false;
  selectedId: number | null = null;
  
  formModel = '';
  formManufacturer = '';
  formStatus = 'ACTIVE';
  
  successMessage = '';
  errorMessage = '';
  isLoading = false;

  constructor(
    public authService: AuthService,
    private apiService: ApiService
  ) {}

  ngOnInit(): void {
    this.loadFleet();
  }

  loadFleet(): void {
    this.isLoading = true;
    this.errorMessage = '';
    
    // If filtering by status
    if (this.filterStatus !== 'ALL') {
      this.apiService.getAircraftByStatus(this.filterStatus).subscribe({
        next: (response) => {
          this.isLoading = false;
          if (response.success) {
            this.aircraftList = response.data || [];
            this.totalPages = 1;
            this.totalElements = this.aircraftList.length;
          }
        },
        error: (err) => {
          this.isLoading = false;
          this.errorMessage = 'Failed to filter aircraft.';
        }
      });
    } 
    // If searching by manufacturer
    else if (this.searchManufacturer.trim()) {
      this.apiService.searchByManufacturer(this.searchManufacturer).subscribe({
        next: (response) => {
          this.isLoading = false;
          if (response.success) {
            this.aircraftList = response.data || [];
            this.totalPages = 1;
            this.totalElements = this.aircraftList.length;
          }
        },
        error: (err) => {
          this.isLoading = false;
          this.errorMessage = err.error?.message || 'Failed to search aircraft.';
        }
      });
    } 
    // Standard paginated retrieval
    else {
      this.apiService.getAllAircraft(this.page, this.size, this.sortBy).subscribe({
        next: (response) => {
          this.isLoading = false;
          if (response.success && response.data) {
            this.aircraftList = response.data.content || [];
            this.totalPages = response.data.totalPages || 0;
            this.totalElements = response.data.totalElements || 0;
          }
        },
        error: (err) => {
          this.isLoading = false;
          this.errorMessage = 'Failed to load aircraft fleet data.';
        }
      });
    }
  }

  // Filter handlers
  onSearchChange(): void {
    this.page = 0;
    this.filterStatus = 'ALL';
    this.loadFleet();
  }

  onFilterStatusChange(status: string): void {
    this.page = 0;
    this.searchManufacturer = '';
    this.filterStatus = status;
    this.loadFleet();
  }

  onSortChange(sortBy: string): void {
    this.sortBy = sortBy;
    this.page = 0;
    this.loadFleet();
  }

  // Pagination triggers
  nextPage(): void {
    if (this.page < this.totalPages - 1) {
      this.page++;
      this.loadFleet();
    }
  }

  prevPage(): void {
    if (this.page > 0) {
      this.page--;
      this.loadFleet();
    }
  }

  // CRUD actions (Admins only)
  openAddForm(): void {
    this.isEditMode = false;
    this.selectedId = null;
    this.formModel = '';
    this.formManufacturer = '';
    this.formStatus = 'ACTIVE';
    this.isFormOpen = true;
    this.successMessage = '';
    this.errorMessage = '';
  }

  openEditForm(aircraft: any): void {
    this.isEditMode = true;
    this.selectedId = aircraft.aircraftId;
    this.formModel = aircraft.model;
    this.formManufacturer = aircraft.manufacturer;
    this.formStatus = aircraft.status;
    this.isFormOpen = true;
    this.successMessage = '';
    this.errorMessage = '';
  }

  closeForm(): void {
    this.isFormOpen = false;
  }

  saveAircraft(): void {
    if (!this.formModel.trim() || !this.formManufacturer.trim() || !this.formStatus) {
      this.errorMessage = 'Please fill out all required fields.';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const payload = {
      model: this.formModel,
      manufacturer: this.formManufacturer,
      status: this.formStatus
    };

    if (this.isEditMode && this.selectedId) {
      this.apiService.updateAircraft(this.selectedId, payload).subscribe({
        next: (response) => {
          this.isLoading = false;
          if (response.success) {
            this.successMessage = 'Aircraft details updated successfully!';
            this.isFormOpen = false;
            this.loadFleet();
          }
        },
        error: (err) => {
          this.isLoading = false;
          this.errorMessage = err.error?.message || 'Failed to update aircraft.';
        }
      });
    } else {
      this.apiService.createAircraft(payload).subscribe({
        next: (response) => {
          this.isLoading = false;
          if (response.success) {
            this.successMessage = 'Aircraft registered successfully!';
            this.isFormOpen = false;
            this.loadFleet();
          }
        },
        error: (err) => {
          this.isLoading = false;
          this.errorMessage = err.error?.message || 'Failed to register aircraft.';
        }
      });
    }
  }

  deleteAircraft(id: number): void {
    if (confirm('Are you sure you want to delete this aircraft?')) {
      this.isLoading = true;
      this.errorMessage = '';
      this.successMessage = '';

      this.apiService.deleteAircraft(id).subscribe({
        next: (response) => {
          this.isLoading = false;
          if (response.success) {
            this.successMessage = 'Aircraft deleted successfully!';
            this.page = 0;
            this.loadFleet();
          }
        },
        error: (err) => {
          this.isLoading = false;
          this.errorMessage = 'Failed to delete aircraft.';
        }
      });
    }
  }
}
