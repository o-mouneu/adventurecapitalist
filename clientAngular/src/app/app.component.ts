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

  constructor(private service: RestserviceService) {
    service.getWorld().then(
      world => {
        this.world = world;
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

}