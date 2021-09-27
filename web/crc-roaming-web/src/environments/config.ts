import { ChartOptions } from "chart.js";
import { Options } from "chartjs-plugin-datalabels/types/options";
import { endWith } from "rxjs/operators";

export const config = {
    regexValidEmail: "^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,4}$",
    regexValidPass:  "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*()?_<>,.]).{8,}$",
    regexOnlyDigits: "^[0-9]*$",

    appVersion: "v.1.5",
    pageSize:   20,
    pageSizeOptions: [20, 50, 100],
    formatTS:   "dd.MM.yyyy HH:mm:ss",
    formatTime: "HH:mm:ss",
    formatDate: "dd.MM.yyyy",

    siteKey: '6Ld4yaIaAAAAAOkuNxEErWfJ1TWVVgNKmN7mM2x9',

    timeoutLogoutMsg:   10 * 60_000,
    timeoutLogout:      60_000,

    resetPasswdAction:  "resetPassword",
    messageAction:      "showMsg",

    polygonColor:       "maroon",
    polygonEditColor:   "fuchsia",
};

export const permissions = {
    viewUsers:      "VIEW_USERS",
    editUsers:      "EDIT_USERS",

    viewRoles:      "VIEW_ROLES",
    editRoles:      "EDIT_ROLES",
    
    viewOperators:  "VIEW_OPERATORS",
    editOperators:  "EDIT_OPERATORS",
    
    viewCountries:  "VIEW_COUNTRIES",
    editCountries:  "EDIT_COUNTRIES",
    
    viewZones:      "VIEW_ZONES",
    editZones:      "EDIT_ZONES",
    
    viewTexts:      "VIEW_TEXTS",
    editTexts:      "EDIT_TEXTS",
    
    viewReports:    "VIEW_REPORTS",
    sendNotifications: "SEND_NOTIFICATIONS",
    viewJournal:    "VIEW_JOURNAL"
};

export const modalMinWidth = {
    minWidth: "25vw",
    maxWidth: "85vw",
    maxHeight: "85vw",
};

export const lineChartOptions: ChartOptions = {
    responsive: true,
    scales: {
        xAxes: [{}],
        yAxes: [{
            ticks: {
                precision: 0
            }
        }],
    },
    plugins: {
        datalabels: {
            align: 'end'
        }
    }
}

export const pieChartOptions: ChartOptions = {
    responsive: true,
    maintainAspectRatio: true,
    plugins: {
        datalabels: {
            anchor: 'end',
            align: (ctx) => {
                if (percent(ctx) > 8) {
                    return 'start';
                } else {
                    return 'end';
                }
            },
            padding: (ctx) => {
                return padding(ctx);
            },
            formatter: (value, ctx) => {
                return percent(ctx).toFixed(2) + "%";
            }
        }
    },
    layout: {
        padding: {
            bottom: 20
        }
    },
}

export const barChartOptions: ChartOptions = {
    responsive: true,
    maintainAspectRatio: true,
    plugins: {
        datalabels: {
            anchor: 'end',
            align: (ctx) => {
                if (percent(ctx) > 8) {
                    return 'start';
                } else {
                    return 'end';
                }
            },
            padding: (ctx) => {
                return padding(ctx);
            },
            formatter: (value, ctx) => {
                return percent(ctx).toFixed(2) + "%";
            }
        }
    },
    layout: {
        padding: {
            bottom: 20
        }
    },
    legend: {
        display: false,
    },
    scales: {
        yAxes: [{
            ticks: {
                beginAtZero: true
            }
        }],
    },
}

function percent(ctx, replaceIndex?: number) {
    let index = replaceIndex != undefined ? replaceIndex : ctx.dataIndex;
    let value = ctx.dataset.data[index] as number;
    let dataArr: any[] = ctx.dataset.data;
    let sum = 0;
    dataArr.map((val: number) => {
        sum += val;
    });
    return (value * 100 / sum);
}

function padding(ctx) {
    if (percent(ctx) > 8) {
        return 3;
    } else if (ctx.dataIndex > 0 && lastIndexLessThanPercent(ctx, 8) == ctx.dataIndex - 1) {
        return 5;
    } else {
        return -1;
    }
}

function lastIndexLessThanPercent(ctx, percentParam) {
    let lastIndex = -1;
    for (let i = 0; i < ctx.dataIndex; i++) {
        if (percent(ctx, i) <= percentParam) {
            lastIndex = i;
        }
    }
    return lastIndex;
}