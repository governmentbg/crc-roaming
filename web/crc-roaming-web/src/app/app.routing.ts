import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ListCountriesComponent } from '@components/list-countries/list-countries.component';
import { ListOperatorsComponent } from '@components/list-operators/list-operators.component';
import { RoamingMapComponent } from '@components/roaming-map/roaming-map.component';
import { ListUsersComponent } from '@components/list-users/list-users.component';
import { ListRolesComponent } from '@components/list-roles/list-roles.component';
import { AuthGuard } from '@helpers/auth.guard';
import { ListTextsComponent } from '@components/list-texts/list-texts.component';
import { ReportRegisteredUsersComponent } from '@components/report-registered-users/report-registered-users.component';
import { ReportedRoamingsComponent } from '@components/reported-roamings/reported-roamings.component';
import { ReportedRoamingsChartComponent } from '@components/reported-roamings-chart/reported-roamings-chart.component';
import { NotificationsComponent } from '@components/notifications/notifications.component';
import { ReportedRoamingsLineChartComponent } from '@components/reported-roamings-line-chart/reported-roamings-line-chart.component';
import { JournalListComponent } from '@components/journal-list/journal-list.component';
import { ReportedRoamingsComparisonChartComponent } from '@components/reported-roamings-comparison-chart/reported-roamings-comparison-chart.component';
import { ReportedBlockingsEventsComponent } from '@components/reported-blockings-events/reported-blockings-events.component';
import { ReportedBlockingsDistributionComponent } from '@components/reported-blockings-distribution/reported-blockings-distribution.component';
import { ReportedBlockingsEvolutionComponent } from '@components/reported-blockings-evolution/reported-blockings-evolution.component';
import { ReportedBlockingsComparisonComponent } from '@components/reported-blockings-comparison/reported-blockings-comparison.component';
import { PrivacyPolicyComponent } from '@components/privacy-policy/privacy-policy.component';
import { CancelledComponent } from '@components/cancelled/cancelled.component';

const routes: Routes = [
    { path: '',                         component: RoamingMapComponent                                       },
    { path: 'privacy-policy',           component: PrivacyPolicyComponent                                    },
    { path: 'zones',                    component: RoamingMapComponent,             canActivate: [AuthGuard] },
    { path: 'countries',                component: ListCountriesComponent,          canActivate: [AuthGuard] },
    { path: 'operators',                component: ListOperatorsComponent,          canActivate: [AuthGuard] },
    { path: 'users',                    component: ListUsersComponent,              canActivate: [AuthGuard] },
    { path: 'roles',                    component: ListRolesComponent,              canActivate: [AuthGuard] },
    { path: 'texts',                    component: ListTextsComponent,              canActivate: [AuthGuard] },
    { path: 'notifications',            component: NotificationsComponent,          canActivate: [AuthGuard] },
    { path: 'journal',                  component: JournalListComponent,            canActivate: [AuthGuard] },
    { path: 'cancelled',                component: CancelledComponent,              canActivate: [AuthGuard] },

    { path: 'report-registered-users',            component: ReportRegisteredUsersComponent,            canActivate: [AuthGuard] },
    { path: 'reported-roamings',                  component: ReportedRoamingsComponent,                 canActivate: [AuthGuard] },
    { path: 'reported-roamings-chart',            component: ReportedRoamingsChartComponent,            canActivate: [AuthGuard] },
    { path: 'reported-roamings-line-chart',       component: ReportedRoamingsLineChartComponent,        canActivate: [AuthGuard] },
    { path: 'reported-roamings-comparison-chart', component: ReportedRoamingsComparisonChartComponent,  canActivate: [AuthGuard] },

    { path: 'reported-blockings-events',          component: ReportedBlockingsEventsComponent,        canActivate: [AuthGuard] },
    { path: 'reported-blockings-distribution',    component: ReportedBlockingsDistributionComponent,  canActivate: [AuthGuard] },
    { path: 'reported-blockings-evolution',       component: ReportedBlockingsEvolutionComponent,     canActivate: [AuthGuard] },
    { path: 'reported-blockings-comparison',      component: ReportedBlockingsComparisonComponent,    canActivate: [AuthGuard] },

];

// configures NgModule imports and exports
@NgModule({
  imports: [RouterModule.forRoot(routes, { relativeLinkResolution: 'legacy' })],
  exports: [RouterModule]
})

export class AppRoutingModule { }
export const appRoutingModule = RouterModule.forRoot(routes, { relativeLinkResolution: 'legacy' });
