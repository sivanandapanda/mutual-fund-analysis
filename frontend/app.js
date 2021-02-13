import "./components/mutual-fund.js";
import "./components/search-mutual-fund.js";
import "./components/search-mf-result.js";
import { dashboardUrl } from './config/api.js';

window.addEventListener('load', () => {
  getMutualFunds();
});

async function getMutualFunds() {
  let myFavSchemeCodes = window.localStorage.getItem("fav-mf-schemecodes");

  if (myFavSchemeCodes && myFavSchemeCodes.length > 2) {
    const res = await fetch(dashboardUrl + JSON.parse(myFavSchemeCodes).join());
    const jsonArr = await res.json();

    const main = document.querySelector('mutualfunds');
    jsonArr.forEach(json => {
      const el = document.createElement('mutual-fund');
      el.mutualFund = json;
      main.appendChild(el);
    });
  }
}