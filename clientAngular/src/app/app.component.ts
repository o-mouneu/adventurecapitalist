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
  logo = this.img+'armoirie-vatican.svg';

  // placeholder value
  // world: World = new World();
  world: World = new World(this.title, this.logo);
  server: string;

  buyQuantities = ["x 1", "x 10", "x 100", "Max"];
  qtmultiIndex = 0;
  qtmulti = this.buyQuantities[this.qtmultiIndex];

  constructor(private service: RestserviceService) {
    this.server = service.server;
    service.getWorld().then(
      world => {
        this.world = world;
      }
    );

  }

  onProductionDone(event){
    this.world.money += event[0] * event[1];
  }

  onBuyDone(event){
    this.world.money -= event;
  }

  switchBuyQuantity(){
    this.qtmultiIndex ++;
    if (this.qtmultiIndex >= this.buyQuantities.length){
      this.qtmultiIndex = 0;
    }
    this.qtmulti = this.buyQuantities[this.qtmultiIndex];
  }

}