import { Component, Input, OnInit, HostListener, Output } from '@angular/core';
import { EventEmitter } from '@angular/core';
import { Pallier, Product } from '../world';

@Component({
  selector: 'app-product',
  templateUrl: './product.component.html',
  styleUrls: ['./product.component.scss']
})

export class ProductComponent implements OnInit {

  progressbarvalue = 0;
  timeleft = 0;
  lastupdate = Date.now();

  // placeholder value because product is not defined

  // placeholder value
  img = '../assets/img/';
  // placeholder value
  logo = this.img+'product-placeholder.png';

  _product: Product;
  _qtmulti: string;
  _buyQuantities: Array<string>;
  _worldMoney: number;
  // _quantityForCostOfBuy : [factor: number, cost: number]
  _quantityForCostOfBuy: Array<number>;

  //_managerUnlocked: boolean = false;
  _onProduction: boolean = false;

  constructor() {
  }
  
  ngOnInit(): void {
    let prod = new Product();
    this.product = prod;
    //this.product.managerUnlocked = false;
    setInterval(() => { this.calcScore(); }, 30);
    this.quantityForCostOfBuy();
  }

  get product(){
    return this._product;
  }

  @Input()
  set product(value: Product) {
    this._product = value;
  }

  @Input()
  set qtmulti(value: string){
    this._qtmulti = value;
    if (this._qtmulti && this.product){
      this.quantityForCostOfBuy();
    }
  }

  get qtmulti(){
    return this._qtmulti;
  }

  // buyQuantities = ["x 1", "x 10", "x 100", "Max"];
  @Input()
  set buyQuantities(value: Array<string>){
    this._buyQuantities = value;
  }

  get buyQuantities(){
    return this._buyQuantities;
  }

  @Input()
  set worldMoney(value: number){
    this._worldMoney = value;
  }

  get worldMoney(){
    return this._worldMoney;
  }

  /*@Input()
  set managerUnlocked(value: boolean){
    this._managerUnlocked = value;
    if (this._managerUnlocked == true){
      this.startFabrication(true);
    }
  }*/

  @Output()
  startManualProduction: EventEmitter<Product> = new EventEmitter<Product>();

  @Output()
  notifyProduction: EventEmitter<Product> = new EventEmitter<Product>();

  @Output()
  notifyBuyProduct: EventEmitter<Product> = new EventEmitter<Product>();

  @Output()
  notifyBuyCost: EventEmitter<number> = new EventEmitter<number>();

  calcScore(){

    if ( this.product.timeleft !=0 ){
      this.product.timeleft = this.product.timeleft - (Date.now() - this.lastupdate);
      this.lastupdate = Date.now();
    }
    if (this.product.timeleft < 0){
      this.product.timeleft = 0;
      this.progressbarvalue = 0;
      this._onProduction = false;
      this.notifyProduction.emit(this.product);
      console.log("notifyProduction sent to app.component");

      console.log("Coût de "+this._quantityForCostOfBuy[0]+" "+this.product.name+":");
      console.log(this._quantityForCostOfBuy[1]);
      console.log("Argent :");
      console.log(this.worldMoney+this.product.revenu*this.product.quantite);

      if( this.product.managerUnlocked ){
        this.startFabrication(true);
      }

    }
    if (this.product.timeleft > 0){
      this.progressbarvalue = ((this.product.vitesse - this.product.timeleft) / this.product.vitesse) * 100;
    }

  }

  startFabrication(auto: boolean){
    if( !this._onProduction && this.product.quantite > 0 ){
      this._onProduction = true;
      if( !auto ){
        this.startManualProduction.emit(this.product);
        console.log("startManualProduction sent to app.component");
      }
      this.product.timeleft = this.product.vitesse;
      this.lastupdate = Date.now();
    }
  }

  buyProduct(){
    if (this.isProductBuyable()){
      this.product.quantite += this._quantityForCostOfBuy[0];
      this.product.cout = this.product.cout * (this.product.croissance ** this._quantityForCostOfBuy[0]);
      this.notifyBuyCost.emit(this._quantityForCostOfBuy[1]);
      console.log("notifyBuyCost sent to app.component");
      this.notifyBuyProduct.emit(this.product);
      console.log("notifyBuyProduct sent to app.component");
    }
  }

  isProductBuyable(){
    return ( this._quantityForCostOfBuy[1] <= this.worldMoney );
  }

  quantityForCostOfBuy(){

    let factor = 1;

    switch(this._qtmulti){
      case this._buyQuantities[0]:
        factor = 1;
        this._quantityForCostOfBuy = [factor, this.calcCostForQuantity( factor )];
        break;
      case this._buyQuantities[1]:
        factor = 10;
        this._quantityForCostOfBuy = [factor, this.calcCostForQuantity( factor )];
        break;
      case this._buyQuantities[2]:
        factor = 100;
        this._quantityForCostOfBuy = [factor, this.calcCostForQuantity( factor )];
        break;
      case this._buyQuantities[3]:
        factor = this.calcMaxCanBuy();
        this._quantityForCostOfBuy = [factor, this.calcCostForQuantity( factor )];
        if  (this._quantityForCostOfBuy[1] == 0 ){
          factor = 1;
          this._quantityForCostOfBuy = [factor, this.calcCostForQuantity( factor )];
        }
        break;
    }

    return this._quantityForCostOfBuy;

  }

  roundedCostOfBuy(){
    return this._quantityForCostOfBuy[1].toFixed(2);
  }

  calcCostForQuantity(factor: number){
    let value = 1;

    value = this.product.cout * ( (1 - this.product.croissance**factor) / (1 - this.product.croissance) );

    return value;
  }

  calcMaxCanBuy(){
    let value = 1;

    let x = 1 - (1 - this.product.croissance) * (this.worldMoney / this.product.cout);
    value = this.logbase(x, this.product.croissance);
    value = Math.floor(value);

    return value;
  }

  logbase(n:number, base:number){
    return Math.log(n)/Math.log(base);
  }

}