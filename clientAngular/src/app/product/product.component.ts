import { Component, Input, OnInit, HostListener } from '@angular/core';
import { Product } from '../world';

@Component({
  selector: 'app-product',
  templateUrl: './product.component.html',
  styleUrls: ['./product.component.scss']
})

export class ProductComponent implements OnInit {

  img = '../assets/img/';  
  logo = this.img+'product-placeholder.png';
  progressbarvalue = 0;
  vitesse = 1000000;
  timeleft = 0;
  lastupdate = Date.now();

  product: Product;
  _prod: Product;

  constructor() {
  }
  
  ngOnInit(): void {
    setInterval(() => { this.calcScore(); }, 30);
  }

  @Input()
  public set prod(value: Product) {
      this.product = value;
  }

  calcScore(){
    if ( this.timeleft !=0 ){
      this.timeleft = this.timeleft - (Date.now() - this.lastupdate);
    }
    if (this.timeleft < 0){
      this.timeleft = 0;
      this.progressbarvalue = 0;
    }
    if (this.timeleft > 0){
      this.progressbarvalue = ((this.vitesse - this.timeleft) / this.vitesse) * 100;
      //this.progressbarvalue = ((this.product.vitesse - this.product.timeleft) / this.product.vitesse) * 100
    }
  }

  startFabrication(){
    this.timeleft = this.vitesse;
    this.lastupdate = Date.now();
  }

}
