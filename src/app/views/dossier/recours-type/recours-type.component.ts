import { AfterViewInit, Component, OnInit, Renderer2 } from "@angular/core";
import { AgGridAngular } from "ag-grid-angular";
import { DossierService } from "../../../service/dossier.service";
import { CommonModule } from "@angular/common";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {
  CardBodyComponent, CardComponent, ColComponent, RowComponent, TextColorDirective
} from "@coreui/angular";
import {
  ClientSideRowModelModule, ColDef, GridReadyEvent, ModuleRegistry,
  NumberFilterModule, TextFilterModule, ValidationModule, PaginationModule,
  DateFilterModule, NumberEditorModule, TextEditorModule, ColumnAutoSizeModule, CellStyleModule, ICellRendererParams
} from "ag-grid-community";
import { Router } from "@angular/router";
import { AuthService } from "../../../service/auth.service"; // Import AuthService

ModuleRegistry.registerModules([
  ColumnAutoSizeModule, NumberEditorModule, TextEditorModule, TextFilterModule,
  NumberFilterModule, PaginationModule, ClientSideRowModelModule, ValidationModule,
  DateFilterModule, CellStyleModule
]);

@Component({
  selector: 'app-recours-type',
  templateUrl: './recours-type.component.html',
  styleUrls: ['./recours-type.component.scss'],
  standalone: true,
  imports: [
    AgGridAngular, CommonModule, TextColorDirective, CardComponent,
    CardBodyComponent, RowComponent, ColComponent, ReactiveFormsModule, FormsModule
  ],
})
export class RecoursTypeComponent implements OnInit, AfterViewInit {
  rowData: any[] = [];
  filteredData: any[] = [];
  loading: boolean = false;
  errorMessage: string | null = null;

  columnDefs: ColDef[] = [
    { headerName: 'Intitulé', field: 'intitule', sortable: true, filter: true, resizable: true },
    { headerName: 'Numéro Dossier', field: 'numeroDossier', sortable: true, filter: true, resizable: true },
    {
      headerName: "État", field: "etat", sortable: true, filter: true,

      cellStyle: (params) => this.getEtatTextColorStyle(params)
    },    { headerName: 'Chargé', field: 'chargeDossier', sortable: true, filter: true, resizable: true },
    {
      headerName: "Date Soumission",
      field: "dateSoumission",
      sortable: true,
      filter: "agDateColumnFilter",
      valueFormatter: (params) => this.formatDate(params.value),
      valueGetter: (params) => params.data?.dateSoumission ? new Date(params.data.dateSoumission) : null,
    },
    {
      headerName: 'Fichiers',
      field: 'fileDetails',
      cellRenderer: (params: ICellRendererParams) => {
        if (!params.value || typeof params.value !== 'object') return '';
        const button = document.createElement('button');
        button.className = 'btn btn-outline-primary btn-sm';
        button.innerText = '📁 Voir ';
        const dossierId = params.data?.id;
        button.addEventListener('click', () => {
          this.router.navigate([`/dossier/dossiers/${dossierId}/fichiers`]);
        });
        const fragment = document.createDocumentFragment();
        fragment.appendChild(button);
        return fragment;
      },
      width: 250,
    },
    {
      headerName: 'Actions',
      field: 'resultat',
      cellRenderer: (params: ICellRendererParams) => {
        const button = document.createElement('button');
        button.className = 'btn btn-warning btn-sm';
        button.innerText = '📝 Details';
        const dossierId = params.data?.id;

        button.addEventListener('click', () => {
          if (dossierId) {
            this.router.navigate([`/dossier/DossierDetails/${dossierId}`]);
          }
        });

        const fragment = document.createDocumentFragment();
        fragment.appendChild(button);
        return fragment;
      },
      width: 200,
    },

    {
      headerName: 'Actions',
      cellRenderer: (params: ICellRendererParams) => {
        const div = document.createElement('div');

        const editButton = document.createElement('button');
        editButton.className = 'btn btn-sm btn-primary edit-btn';
        editButton.innerText = 'Modifier';
        const dossierId = params.data?.id;
        editButton.addEventListener('click', () => {
          this.router.navigate([`/dossier/edit-dossier/${dossierId}`]);
        });

        const deleteButton = document.createElement('button');
        deleteButton.className = 'btn btn-danger btn-sm';
        deleteButton.innerText = 'Supprimer';
        deleteButton.addEventListener('click', () => {
          if (confirm('Êtes-vous sûr de vouloir supprimer ce dossier ?')) {
            this.dossierService.deleteDossier(dossierId).subscribe({
              next: () => this.getDossiersByType(),
              error: (error) => {
                console.error('Erreur lors de la suppression du dossier :', error);
                alert("Erreur lors de la suppression du dossier.");
              }
            });
          }
        });

        div.appendChild(editButton);
        div.appendChild(deleteButton);
        return div;
      },
      width: 250,
      suppressSizeToFit: true,
    }

  ];

  getEtatTextColorStyle(params: any): any {
    if (params.value === 'EN_ATTENTE') {
      return { 'color': '#ffeb3b', 'font-weight': 'bold' };  // Jaune
    } else if (params.value === 'TRAITE') {
      return { 'color': '#4caf50', 'font-weight': 'bold' };  // Vert
    } else if (params.value === 'EN_TRAITEMENT') {
      return { 'color': '#0d0795', 'font-weight': 'bold' };  // Rouge
    }
    return {};
  }
  defaultColDef = { flex: 1, minWidth: 120, resizable: true };
  paginationPageSize = 10;
  paginationPageSizeSelector = [1, 5, 10];

  constructor(
    private dossierService: DossierService,
    private router: Router,
    private renderer: Renderer2,
    private authService: AuthService // Import AuthService
  ) {}

  ngOnInit(): void {
    this.getDossiersByType();
  }

  ngAfterViewInit(): void {
    this.addActionListeners();
  }

  getDossiersByType(): void {
    this.loading = true;
    this.errorMessage = null;

    this.dossierService.getDossiersByType("RECOURS").subscribe(
      (data: any) => { // Expecting either an array or an object with 'dossiers'
        console.log("✅ Données RECOURS reçues :", data);
        let dossiersArray: any[] = [];

        if (Array.isArray(data)) {
          dossiersArray = data;
        } else if (data && data.dossiers && Array.isArray(data.dossiers)) {
          dossiersArray = data.dossiers;
        } else {
          this.rowData = [];
          this.errorMessage = "Erreur: Format de données RECOURS invalide.";
          console.error("❌ Format de données RECOURS invalide :", data);
          this.loading = false;
          return;
        }

        this.rowData = dossiersArray.map((dossier: any) => ({
          id: dossier.id,
          intitule: dossier.intitule,
          numeroDossier: dossier.numeroDossier,
          typePassation: dossier.typePassation,
          dateSoumission: dossier.dateSoumission,
          fileDetails: dossier.fileDetails,
          chargeDossier: dossier.chargeDossier?.name || 'N/A',

          // Champs extraits depuis "details"
          etat: dossier.etat

        }));
        console.log("✅ rowData RECOURS après mapping :", this.rowData);
        this.loading = false;
      },
      (error) => {
        console.error('❌ Erreur lors de la récupération des dossiers RECOURS :', error);
        this.loading = false;
        this.errorMessage = 'Erreur lors de la récupération des données RECOURS.';
      }
    );
  }

  private formatDate(date: string | null): string {
    if (!date) return "N/A";
    const parsedDate = new Date(date);
    return isNaN(parsedDate.getTime()) ? "N/A" : parsedDate.toLocaleDateString("fr-FR", {
      year: "numeric",
      month: "long",
      day: "numeric"
    });
  }

  addActionListeners() {
    const table = document.querySelector("ag-grid-angular");
    if (table) {
      this.renderer.listen(table, "click", (event: Event) => {
        const target = event.target as HTMLElement;
        if (target.tagName === "A") {
          alert(`Téléchargement de : ${target.innerText}`);
        }
      });
    }
  }

  onSearch(event: Event) {
    const target = event.target as HTMLInputElement;
    if (!target || !target.value) {
      this.filteredData = [...this.rowData];
      return;
    }

    const query = target.value.toLowerCase();

    this.filteredData = this.rowData.filter(dossier =>
      (dossier.numeroDossier && dossier.numeroDossier.toLowerCase().includes(query)) ||
      (dossier.intitule && dossier.intitule.toLowerCase().includes(query)) ||
      (dossier.typePassation && dossier.typePassation.toLowerCase().includes(query)) ||
      (dossier.dateSoumission && this.formatDate(dossier.dateSoumission).toLowerCase().includes(query)) ||
      (dossier.etat && dossier.etat.toLowerCase().includes(query)) ||
      (typeof dossier.fileDetails === 'object' && Object.keys(dossier.fileDetails).some(fileName => fileName.toLowerCase().includes(query))) ||
      (dossier.chargeDossier && dossier.chargeDossier.toLowerCase().includes(query))
    );
  }

  onGridReady(params: GridReadyEvent) {
    params.api.sizeColumnsToFit();
  }
  selectedType: string = '';

  onTypeChange(): void {
    if (this.selectedType) {
      const encodedType = encodeURIComponent(this.selectedType);
      this.router.navigate([`/dossier/${encodedType}`]);
    }
  }
}
