import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private readonly baseUrl = 'http://localhost:8080';

  constructor(private http: HttpClient, private authService: AuthService) {}

  private getHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });
  }

  login(body: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/api/auth/login`, body);
  }

  getAllAircraft(page: number, size: number, sortBy: string): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy);
    return this.http.get(`${this.baseUrl}/api/aircraft`, {
      headers: this.getHeaders(),
      params
    });
  }

  createAircraft(body: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/api/aircraft`, body, { headers: this.getHeaders() });
  }

  updateAircraft(id: number, body: any): Observable<any> {
    return this.http.put(`${this.baseUrl}/api/aircraft/${id}`, body, { headers: this.getHeaders() });
  }

  deleteAircraft(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/api/aircraft/${id}`, { headers: this.getHeaders() });
  }

  getAircraftByStatus(status: string): Observable<any> {
    return this.http.get(`${this.baseUrl}/api/aircraft/status/${status}`, { headers: this.getHeaders() });
  }

  searchByManufacturer(manufacturer: string): Observable<any> {
    const params = new HttpParams().set('manufacturer', manufacturer);
    return this.http.get(`${this.baseUrl}/api/aircraft/search`, {
      headers: this.getHeaders(),
      params
    });
  }

  createEngineer(body: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/api/users/engineers`, body, { headers: this.getHeaders() });
  }
}
