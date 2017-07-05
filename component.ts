import { Component } from '@angular/core';
import { NavController } from 'ionic-angular';
import { Device } from '@ionic-native/device';


@Component({
  selector: 'page-home',
  templateUrl: 'home.html',
  providers: [Device]
})
export class HomePage {
public mobileId : {};
  constructor(public navCtrl: NavController,public device : Device) {
    this.mobileId = JSON.parse(this.device.uuid);
  }

}