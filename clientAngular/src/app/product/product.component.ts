import { Component, Input, OnInit } from '@angular/core';
import { Product } from '../world';

@Component({
  selector: 'app-product',
  templateUrl: './product.component.html',
  styleUrls: ['./product.component.scss']
})



export class ProductComponent implements OnInit {

  img = '../assets/img/';  
  logo = this.img+'product-placeholder.png'; 

  product: Product;
  _prod: Product;

  constructor() {
  }
  
  ngOnInit(): void {
    throw new Error('Method not implemented.');
  }

  @Input()
  public set prod(value: Product) {
      this.product = value;
  }

}
