import { html, render } from 'https://unpkg.com/lit-html?module';
import { dashboardUrl } from '../config/api.js';

class SearchMutualFundResult extends HTMLElement {
    set searchResult({ schemeName, schemeCode }) {
        const template = html`
            <div class="mf-result">
                <p>${schemeName}</p>
                <button type="submit" @click="${_ => this._handleClick(schemeCode)}"><i class="fas fa-plus-circle"></i></button>
            </div>
        `;

        render(template, this);
    }

    async _handleClick(schemeCode) {
        let favMfSchemeCodes = JSON.parse(window.localStorage.getItem("fav-mf-schemecodes")) || [];

        if (!favMfSchemeCodes.includes(schemeCode)) {
            favMfSchemeCodes.push(schemeCode);
            window.localStorage.setItem("fav-mf-schemecodes", JSON.stringify(favMfSchemeCodes));

            const res = await fetch(dashboardUrl + schemeCode);
            const jsonArr = await res.json();

            const main = document.querySelector('mutualfunds');
            jsonArr.forEach(json => {
                const el = document.createElement('mutual-fund');
                el.mutualFund = json;
                main.prepend(el);
            });
        }
    }
}

customElements.define("search-mfresult", SearchMutualFundResult);