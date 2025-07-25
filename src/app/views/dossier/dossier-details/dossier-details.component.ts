import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { DossierService } from '../../../service/dossier.service';
import { UserService } from '../../../service/user.service';

import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatCard, MatCardContent, MatCardHeader, MatCardTitle } from '@angular/material/card';
import { MatIcon } from '@angular/material/icon';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { HttpClient } from '@angular/common/http';
import { MatProgressBar } from '@angular/material/progress-bar';
import { MatList, MatListItem } from '@angular/material/list';
import { MatTabsModule } from '@angular/material/tabs';

import { forkJoin, of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';
import {DomSanitizer} from "@angular/platform-browser";
import { DossierFilesComponent } from '../dossier-files/dossier-files.component'; // <--- Import the new component

@Component({
  selector: 'app-dossier-details',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatCheckboxModule,
    MatCard,
    MatCardHeader,
    MatCardTitle,
    MatCardContent,
    MatIcon,
    MatSlideToggleModule,
    MatProgressBar,
    MatList,
    MatListItem,
    MatTabsModule,
    DossierFilesComponent
  ],
  templateUrl: './dossier-details.component.html',
  styleUrl: './dossier-details.component.scss'
})
export class DossierDetailsComponent implements OnInit {
  dossierDetails: any;
  errorMessage: string | null = null;
  dossierId!: number;
  selectedPdfUrl: string | null = null

  constructor(
    private route: ActivatedRoute,
    private dossierService: DossierService,
    private userService: UserService,
    private _formBuilder: FormBuilder,
    private sanitizer: DomSanitizer,
    private router: Router,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    this.dossierId = idParam ? +idParam : 0;
    this.loadDossierDetails();
    this.dossierDetails = {
      fileDetails: {
        'Contrat_Client_XYZ.pdf': 'https://vadimdez.github.io/ng2-pdf-viewer/assets/pdf-sample.pdf', // Example public PDF URL
        'Facture_2025_06_01.pdf': 'https://mozilla.github.io/pdf.js/web/compressed.tracemonkey-pldi-09.pdf', // Another example
        'Image_Projet_A.jpg': 'https://via.placeholder.com/150', // Non-PDF file
        'Rapport_Annuel.pdf': 'https://www.africau.edu/images/default/sample.pdf' // Another example
      }
    };
  }

  /**
   * Charge les détails du dossier + les décisions associées avec enrichissement utilisateur.
   */
  loadDossierDetails(): void {
    this.errorMessage = null;

    const dossierDetails$ = this.dossierService.getDossierById(this.dossierId).pipe(
      catchError(error => {
        console.error('❌ Erreur chargement dossier:', error);
        this.errorMessage = 'Erreur lors du chargement des détails du dossier.';
        return of(null);
      })
    );

    const decisions$ = this.http.get<any[]>(`http://localhost:8085/api/decisions/dossiers/${this.dossierId}`).pipe(
      // Étape 1 : conversion de dateAjout (string → Date)
      map(decisions => decisions.map(decision => {
        if (decision.dateAjout) {
          const date = new Date(decision.dateAjout);
          decision.dateAjout = isNaN(date.getTime()) ? null : date;
        } else {
          decision.dateAjout = null;
        }
        return decision;
      })),
      // Étape 2 : enrichissement du nom du chargé de dossier
      switchMap(decisions => {
        const enrichments = decisions.map(decision => {
          if (decision.chargeDossierId) {
            return this.userService.getUserById(decision.chargeDossierId).pipe(
              map(user => {
                decision.chargeDossierName = user?.name || 'Inconnu';
                return decision;
              }),
              catchError(() => {
                decision.chargeDossierName = 'Inconnu';
                return of(decision);
              })
            );
          }
          decision.chargeDossierName = 'Inconnu';
          return of(decision);
        });

        return forkJoin(enrichments);
      }),
      catchError(error => {
        console.error('❌ Erreur chargement décisions:', error);
        return of([]);
      })
    );

    forkJoin([dossierDetails$, decisions$]).subscribe({
      next: ([dossier, decisions]) => {
        if (dossier) {
          this.dossierDetails = dossier;
          this.dossierDetails.decisions = decisions;
        } else {
          this.errorMessage = 'Dossier non trouvé.';
        }
      },
      error: () => {
        this.errorMessage = 'Erreur inattendue lors du chargement.';
      }
    });
  }

  /**
   * Redirige vers la liste des dossiers
   */
  onCancel(): void {
    this.router.navigate(['/dossier/dossier']);
  }
  generatePdf(): void {
    const dossierId = this.dossierDetails?.dossier?.id;
    if (dossierId) {
      const url = `http://localhost:9091/generate-allpdf/${dossierId}`;
      window.open(url, '_blank');
    }
  }
  generateSingleDossierPdf() {
    window.open(`http://localhost:9091/generate-dossier-pdf/${this.dossierId}`, '_blank');
  }
  openFile(fileIdOrUrl: string) {
    let fileUrl: string;
    // Vérifiez si la valeur ressemble déjà à une URL
    if (fileIdOrUrl.startsWith('http://') || fileIdOrUrl.startsWith('https://')) {
      fileUrl = fileIdOrUrl;
    } else {
      // Si ce n'est pas une URL, assumez que c'est un ID et construisez l'URL
      fileUrl = `http://localhost:8083/api/attachments/view/${fileIdOrUrl}`;
    }
    console.log('🔗 Ouvrir l\'URL :', fileUrl);
    window.open(fileUrl, '_blank');
  }


}
