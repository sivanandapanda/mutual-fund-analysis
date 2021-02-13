import { html, render } from 'https://unpkg.com/lit-html?module';

class MutualFund extends HTMLElement {
    set mutualFund({ mutualFundStatistics: { mutualFundMeta, statistics, percentageIncrease } }) {
        const template = html`
            <div class="mutual-fund">
                <div class="mutual-fund-header">
                    <h6>${mutualFundMeta.fundHouse} - ${mutualFundMeta.schemeName} &nbsp;&nbsp;&nbsp;&nbsp;<small>Category: ${mutualFundMeta.schemeCategory}</small>&nbsp;&nbsp;&nbsp;&nbsp; Last 5y % increase= ${percentageIncrease}%</h6>
                    <button type="submit" @click="${_ => this._handleClick(mutualFundMeta)}"><i class="fas fa-minus-circle"></i></button>
                </div>

                <div class="statistics">
                    ${statistics.statisticsList.map(stat => 
                    html`<div class="statistics-grid"> 
                        <div> 
                            ${stat ? html`${stat.date[2]}/${stat.date[1]}/${stat.date[0]} 
                        </div> 
                        <div> 
                        ${stat.nav} ${this.getMoveIcon(stat)}` : html``} 
                        </div> 
                    </div>`
                    )}
                </div>

            </div>
        `;
        render(template, this);
    }

    getMoveIcon(stat) {
        if (stat.move === "UP") {
            return html`<i class="fas fa-sort-amount-up-alt" style="color:green;"></i>`;
        } else if (stat.move === "DOWN") {
            return html`<i class="fas fa-sort-amount-down-alt" style="color:red;"></i>`;
        } else {
            return html``;
        }
    }

    _handleClick(mutualFundMeta) {
        if(window.localStorage.getItem("fav-mf-schemecodes")) {
            let favMfSchemeCodes = JSON.parse(window.localStorage.getItem("fav-mf-schemecodes"));

            window.localStorage.setItem("fav-mf-schemecodes", JSON.stringify(favMfSchemeCodes.filter(code => code !== mutualFundMeta.schemeCode)));
        }

        document.querySelector('mutualfunds').childNodes.forEach(node => {
            if(node.textContent.indexOf(mutualFundMeta.schemeCategory) > 0 && node.textContent.indexOf(mutualFundMeta.schemeName) > 0) {
                node.remove();
            }
        });
    }
}

customElements.define('mutual-fund', MutualFund);