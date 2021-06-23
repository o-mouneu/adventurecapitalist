import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { World, Pallier, Product } from './world';

@Injectable({
  providedIn: 'root'
})
export class RestserviceService {

  _server: string = "http://localhost:8080/"
  //_server: string = "http://10.157.5.33:8080/";
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

  private setHeaders(user: string): HttpHeaders {
    var headers = new HttpHeaders({ 'X-User': user});
    return headers;
  }

  private handleError(error: any): Promise<any> {
    console.error('An error occurred', error);
    return Promise.reject(error.message || error);
  }
  
  getWorld(): Promise<World> {
    console.log("getWorld triggered avec X-user : "+this.user);
    return this.http.get(this._server + "adventureisis/generic/world",
    { headers: this.setHeaders(this.user)})
    .toPromise().catch(this.handleError);
  }

  deleteWorld(): Promise<World> {
    return this.http.delete(this._server + "adventureisis/generic/world",
    { headers: this.setHeaders(this.user)})
    .toPromise().catch(this.handleError);
  }

  putProduct(product: Product){
    console.log("putProduct triggered avec X-user : "+this.user);
    console.log(product);
    this.http.put<Product>(this._server + "adventureisis/generic/product", product,
    { headers: this.setHeaders(this.user)})
    .toPromise().catch(this.handleError);
  }

}
