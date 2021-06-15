
export class World {
    name : string;
    logo : string;
    money: number; 
    score: number; 
    totalangels: number;
    activeangels: number;
    angelbonus: number;
    lastupdate: string;
    products : { "product": Product[] };
    allunlocks: { "pallier": Pallier[]};
    upgrades: { "pallier": Pallier[]};
    angelupgrades: { "pallier": Pallier[]};
    managers: { "pallier": Pallier[]};

    constructor(name:string, logo:string) {
        // placeholder value
        this.money = 0;
        // placeholder value
        this.name = name;
        // placeholder value
        this.logo = logo;
        this.products = { "product":[ ] } ;
        this.managers = { "pallier":[ ] };
        this.upgrades = { "pallier":[ ] };
        this.angelupgrades = { "pallier":[ ] };
        this.allunlocks = { "pallier":[ ] };
    }
}

export class Product {
    id : number;
    name : string;
    logo : string;
    cout : number;
    croissance: number;
    revenu: number;
    vitesse: number;
    quantite: number;
    timeleft: number;
    managerUnlocked: boolean;
    palliers : { "pallier" : Pallier[]};
    
    constructor(){
        this.revenu = 1;
        this.cout = 1;
    }

}

export class Pallier {
    name: string;
    logo: string;
    seuil: number;
    idcible: number;
    ratio: number;
    typeratio: string;
    unlocked: boolean;
}
