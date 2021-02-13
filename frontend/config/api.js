const rootUrl = 'http://localhost:8080';
//const rootUrl = 'http://web-sivananda20-dev.apps.sandbox.x8i5.p1.openshiftapps.com';

const dashboardInitUrl = rootUrl + 
"/mf/api/v1/dashboard/create?schemeCodes=141245,141244,141243,141246,141463,141466,141465,141464,141701,141700,141699,141698,139862,139861,139859,139860,140065,140068,140067,140066,143088,143085,143087,143086,135304,135302,135303,135301,135358,135359,135357,135360,135512,135513,135510,135511,135861,135863,135862,135860,136123,136124,136121,136122,136418,136419,136417,136420,140654,140655,140657,140656,124595,124596,106338,106337,125077,106709,106707,125078,106706,126030";

const dashboardUrl = rootUrl + "/mf/api/v1/dashboard/create?schemeCodes=";

const mutualFundSearchUrl = rootUrl + "/mf/api/v1/mutualfund/search?schemeName=";

const mutualFundExploreUrl = rootUrl + "/mf/api/v1/mutualfund/explore?schemeName=";

export { dashboardUrl, dashboardInitUrl, mutualFundSearchUrl, mutualFundExploreUrl };