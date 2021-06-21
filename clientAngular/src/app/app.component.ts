import { Component } from '@angular/core';
import { RestserviceService } from './restservice.service';
import { World, Pallier, Product } from './world';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  
  // placeholder value
  title = 'Vatican Capitalist';
  // placeholder value
  img = 'assets/img/';
  // placeholder value
  _logo = this.img+'armoirie-vatican.svg';

  world: World = new World();
  server: string;

  buyQuantities = ["x 1", "x 10", "x 100", "Max"];
  qtmultiIndex = 0;
  qtmulti = this.buyQuantities[this.qtmultiIndex];

  _showManagers = false;

  username: any;

  constructor(private service: RestserviceService) {

    this.username = localStorage.getItem("username");
    console.log("username : "+this.username);

    if(this.username == ""){
      this.username = this.generateRandomUsername();
      localStorage.setItem("username", this.username);
      console.log("username was null, set to : "+this.username);
    }

    service.user = this.username;

    service.getWorld().then(
      world => {
        console.log("getting world...");
        this.world = world;
        console.log("world got :");
        console.log(this.world);
      }
    );

  }

  get logo(){
    return this._logo;
  }

  public get showManagers(){
    return this._showManagers;
  }

  public set showManagers(value:boolean){
    this._showManagers = value;
  }

  onManualProductionStarted(product: Product){
    this.service.putProduct(product);
    console.log("onManualProductionStarted sent to server:");
    console.log(product);
  }

  onProductionDone(product: Product){
    this.world.money += product.quantite * product.revenu;
  }

  onBuyDoneProduct(product: Product){
    this.service.putProduct(product);
  }

  onBuyDoneCost(costOfBuy: number){
    this.world.money -= costOfBuy;
  }

  switchBuyQuantity(){
    this.qtmultiIndex ++;
    if (this.qtmultiIndex >= this.buyQuantities.length){
      this.qtmultiIndex = 0;
    }
    this.qtmulti = this.buyQuantities[this.qtmultiIndex];
  }

  hireManager(manager){

  }

  generateRandomUsername(){
    let titre = ["Abbe","Fidele","Frere"];
    let nom = ["Pierre","Jean","Marc"];
    let ID = Math.floor(Math.random() * 100000);
    let pseudo = titre[this.randIndex(titre)] + "-" + nom[this.randIndex(nom)] + "-" + ID;

    return pseudo;
  }

  onUsernameChanged(){

    if(this.username == ""){
      this.username = this.generateRandomUsername();
      console.log("username was null, set to : "+this.username);
    }

    console.log("username changed : "+this.username);
    localStorage.setItem("username", this.username);
    //this.username = localStorage.getItem("username");
    console.log("username stored locally : "+this.username);
    this.service.user = this.username;
  }

  randIndex(array){
    return Math.floor(Math.random() * array.length);
  }

}