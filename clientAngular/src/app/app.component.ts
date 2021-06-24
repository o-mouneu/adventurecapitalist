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
  _showUpgrades = false;

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


  public get showUpgrades(){
    return this._showUpgrades;
  }

  public set showUpgrades(value:boolean){
    this._showUpgrades = value;
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
    if(this.world.money >= manager.seuil){
      this.world.money -= manager.seuil;
      manager.unlocked = true;
      this.world.products.product[manager.idcible-1].managerUnlocked = true;
    }
  }

  /*
    Buy upgrade
  */
  buyUpgrade(upgrade : Pallier){
    if(this.world.money >= upgrade.seuil){
      this.world.money -= upgrade.seuil;
      upgrade.unlocked = true;
    }
    this.applyUpgrade(upgrade);
    this.service.putUpgrade(upgrade);
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


  findProductById(idCible : number) {
    for(let p=0; p<this.world.products.product.length; p++ ) {
      if( this.world.products.product[p].id == idCible )
        return this.world.products.product[p];
    }
    return null;
  }


  applyUpgrade(pallier : Pallier) {
    if( pallier.typeratio == "anges") {

    } else {
      let idCible = pallier.idcible;
      let bonusVitesse = 1;
      let bonusGain = 1;

      if( pallier.typeratio == "vitesse") 
        bonusVitesse = pallier.ratio;
      
      if( pallier.typeratio == "gain") 
        bonusGain = pallier.ratio;
      
      console.log(" BONUS : " + bonusVitesse + " / " + bonusGain);

      if( idCible == 0 ) {
        for(let p=0; p<this.world.products.product.length; p++ ) {
          this.world.products.product[p].revenu /= bonusGain;
          this.world.products.product[p].vitesse /= bonusVitesse;
        }
      } else {
        let productcible = this.findProductById(idCible);
        if( productcible != null ) {
          productcible.revenu /= bonusGain;
          productcible.vitesse /= bonusVitesse;
          console.log("Bonus applied");
        } else {
          console.log("Produit introuvable");
        }
      }
    }
     
  }

}