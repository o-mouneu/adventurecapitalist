import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { World, Pallier, Product } from './world';

@Injectable({
  providedIn: 'root'
})
export class RestserviceService {

  _server: string = "http://localhost:8080/";
  _user: string = "";

  constructor(private http: HttpClient) { }

  public get user(){
    return this._user;
  }

  public set user(user: string){
    this._user = user;
  }

  public get server(){
    return this._server;
  }

  private handleError(error: any): Promise<any> {
    console.error('An error occurred', error);
    return Promise.reject(error.message || error);
  }
  
  getWorld(): Promise<World> {
    return this.http.get(this._server + "adventureisis/generic/world")
    .toPromise().catch(this.handleError);
  }

}