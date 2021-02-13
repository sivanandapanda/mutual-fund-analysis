import { html, render } from 'https://unpkg.com/lit-html?module';
import { mutualFundSearchUrl, mutualFundExploreUrl } from '../config/api.js';

class SearchMutualFund extends HTMLElement {
    connectedCallback() {
        const template = html`
            <h2>Search Mutual Funds</h2>
            <div class="search-mf-form">
                <input type="text" name="schemeNameTag" id="schemeNameTag" placeholder="Search Mutual Funds...">
                <button type="submit" @click="${this._handleClick}"><i class="fas fa-search"></i></button>
                <button type="explore" @click="${this._handleExploreClick}"><i class="fab fa-wpexplorer"></i></button>
            </div>
            <div>
                <searchmfresults></searchmfresults>
            </div>
        `;

        render(template, this);
    }

    async _handleClick() {
        const main = document.querySelector('searchmfresults');
        main.innerHTML = '';

        let schemeNameTagNode = document.getElementById("schemeNameTag");
        const schemeName = schemeNameTagNode.value;

        const res = await fetch(mutualFundSearchUrl + schemeName);
        const jsonArr = await res.json();

        if(jsonArr) {
            jsonArr.forEach(json => {
                const el = document.createElement('search-mfresult');
                el.searchResult = json;
                main.appendChild(el);
            });
        }
    }

    async _handleExploreClick() {
        const main = document.querySelector('searchmfresults');
        main.innerHTML = '';

        let schemeNameTagNode = document.getElementById("schemeNameTag");
        const schemeName = schemeNameTagNode.value;

        const res = await fetch(mutualFundExploreUrl + schemeName);
        const jsonArr = await res.json();

        const mutualfundsDoc = document.querySelector('mutualfunds');
        mutualfundsDoc.innerHTML = '';
        jsonArr.forEach(json => {
            const el = document.createElement('mutual-fund');
            el.mutualFund = json;
            mutualfundsDoc.prepend(el);
        });
    }
}

customElements.define("search-mutualfund", SearchMutualFund);