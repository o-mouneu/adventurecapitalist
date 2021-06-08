import { Component } from '@angular/core';
import { RestserviceService } from './restservice.service';
import { World, Pallier, Product } from './world';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  title = 'Vatican Capitalist';

  world: World = new World();
  server: string;

  constructor(private service: RestserviceService) {
    this.server = service.server;
    service.getWorld().then(
      world => {
        this.world = world;
      }
    );
  }

}