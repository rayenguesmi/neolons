import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from './AuthGuard';
import { AdministrationComponent } from './administration/administration.component';
import { AuditSuiviComponent } from './audit-suivi/audit-suivi.component';
import { GestionCampComponent } from './gestion-camp/gestion-camp.component';
import { GestionDocComponent } from './gestion-doc/gestion-doc.component';
import { GestionExecComponent } from './gestion-exec/gestion-exec.component';
import { GestionLicencesComponent } from './gestion-licences/gestion-licences.component';
import { GestionUtilisateurComponent } from './gestion-utilisateur/gestion-utilisateur.component';
import { GestionRolesComponent } from './gestion-roles/gestion-roles.component';
import { HomeComponent } from './home/home.component';
import { LandingComponent } from './landing/landing.component';

const routes: Routes = [
  {
    path: '',
    component: LandingComponent,
    canActivate: [AuthGuard],
    children: [
      { 
        path: 'home', 
        component: HomeComponent,
        canActivate: [AuthGuard]
      },
      { 
        path: 'gestionnaire-de-doc', 
        component: GestionDocComponent,
        canActivate: [AuthGuard]
      },
      { 
        path: 'g-de-campagne', 
        component: GestionCampComponent,
        canActivate: [AuthGuard]
      },
      { 
        path: 'g-dexecution', 
        component: GestionExecComponent,
        canActivate: [AuthGuard]
      },
      { 
        path: 'administration', 
        component: AdministrationComponent,
        canActivate: [AuthGuard]
      },
      { 
        path: 'gestion-licences', 
        component: GestionLicencesComponent,
        canActivate: [AuthGuard]
      },
      { 
        path: 'gestion-utilisateur', 
        component: GestionUtilisateurComponent,
        canActivate: [AuthGuard]
      },
      { 
        path: 'gestion-roles', 
        component: GestionRolesComponent,
        canActivate: [AuthGuard]
      },
      { 
        path: 'audit-suivi', 
        component: AuditSuiviComponent,
        canActivate: [AuthGuard]
      }
    ],
  },
  { 
    path: '**', 
    redirectTo: '' 
  }
];

@NgModule({
  declarations: [],
  imports: [RouterModule.forRoot(routes), CommonModule],
  exports: [RouterModule],
})
export class AppRoutingModule {}


