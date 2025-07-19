import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class ContextService {
  constructor(private http: HttpClient) {}
  private subject = new Subject<any>();
  baseUrl = 'http://localhost:8090';

  public getAllContexts(): Observable<any> {
    return this.http.get(this.baseUrl + '/context/all');
  }
}
