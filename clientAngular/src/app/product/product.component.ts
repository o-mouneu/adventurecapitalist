import { Component, Input, OnInit } from '@angular/core';
import { Product } from '../world';

@Component({
  selector: 'app-product',
  templateUrl: './product.component.html',
  styleUrls: ['./product.component.scss']
})



export class ProductComponent implements OnInit {

  product: Product = new Product();

  constructor() {
  }
  ngOnInit(): void {
    throw new Error('Method not implemented.');
  }

  /*@Input()
  set prod(value: Product) {
      this.product = value;
    }
  }*/

  /*ngOnInit(): void {
  }*/

}
