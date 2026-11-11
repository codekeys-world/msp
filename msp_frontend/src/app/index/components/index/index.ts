import { Component } from '@angular/core';
import { Header } from "../header/header";
import { Content } from "../content/content";
import { Footer } from "../footer/footer";

@Component({
  selector: 'app-index',
  imports: [Header, Content, Footer],
  templateUrl: './index.html',
  styleUrl: './index.scss',
})
export class Index {}
