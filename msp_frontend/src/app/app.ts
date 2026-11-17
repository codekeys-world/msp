import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {CookieBanner} from './shared/components/cookie-banner';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, CookieBanner],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  protected readonly title = signal('msp_frontend');
}
