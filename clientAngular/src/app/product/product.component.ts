import { Component, Input, OnInit, HostListener, Output } from '@angular/core';
import { EventEmitter } from '@angular/core';
import { Product } from '../world';

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
  // placeholder value
  vitesse = 200;
  // placeholder value
  cout = 1;
  // placeholder value
  croissance = 1.01;
  // placeholder value
  revenu = 1000;
  // placeholder value
  quantite = 1;

  coutInitial = this.cout;

  product: Product;
  _prod: Product;
  _qtmulti: string;
  _buyQuantities: Array<string>;
  _worldMoney: number;
  // _quantityForCostOfBuy : [factor: number, cost: number]
  _quantityForCostOfBuy: Array<number>;

  constructor() {
  }
  
  ngOnInit(): void {
    this.quantityForCostOfBuy();
    setInterval(() => { this.calcScore(); }, 30);
  }

  @Input()
  public set prod(value: Product) {
      this.product = value;
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

  @Output()
  // notifyProduction: EventEmitter<Product> = new EventEmitter<Product>();
  // placeholder value
  notifyProduction: EventEmitter<Array<number>> = new EventEmitter<Array<number>>();

  @Output()
  notifyBuy: EventEmitter<number> = new EventEmitter<number>();

  calcScore(){

    if ( this.timeleft !=0 ){
      this.timeleft = this.timeleft - (Date.now() - this.lastupdate);
      this.lastupdate = Date.now();
    }
    if (this.timeleft < 0){
      this.timeleft = 0;
      this.progressbarvalue = 0;
      // this.notifyProduction.emit(this.product);
      // placeholder value
      this.notifyProduction.emit([this.quantite,this.revenu]);
      this.isProductBuyable();
    }
    if (this.timeleft > 0){
      this.progressbarvalue = ((this.vitesse - this.timeleft) / this.vitesse) * 100;
    }

  }

  startFabrication(){
    this.timeleft = this.vitesse;
    this.lastupdate = Date.now();
  }

  buyProduct(){
    if (this.isProductBuyable()){
      this.quantite += this._quantityForCostOfBuy[0];
      this.cout = this.cout * (this.croissance ** this._quantityForCostOfBuy[0]);
      this.notifyBuy.emit(this._quantityForCostOfBuy[1]);
    }
  }

  isProductBuyable(){
    return ( this._quantityForCostOfBuy[1] <= this.worldMoney);
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

  // FORMULE NE VA PAS : RETOURNE cout x (r^n) mais ne prend pas en compte les éléments précédents de la suite géométrique
  calcCostForQuantity(factor: number){
    let value = 1;

    value = this.cout * ( (1 - this.croissance**factor) / (1 - this.croissance) );

    return value;
  }

  calcMaxCanBuy(){
    let value = 1;

    let x = 1 - (1 - this.croissance) * (this.worldMoney / this.cout);
    value = this.logbase(x, this.croissance);
    value = Math.floor(value);

    return value;
  }

  logbase(n:number, base:number){
    return Math.log(n)/Math.log(base);
  }

}