		</div>
    </div>
    
    <section>
      <div class="container">
        <p class="muted credit">&copy; Scribble.org, 2015 | Baked with <a href="http://jbake.org">JBake ${version}</a></p>
    <p id="forkongithubp"><span id="forkongithub">
      <a href="https://github.com/scribble" class="bg-grey">
        Fork me on GitHub
      </a>
    </span></p>
      </div>
    </section>
    
    <!-- javascript -->
    <!-- Placed at the end of the document so the pages load faster -->
    <!-- js -->
    <script src="<#if (content.rootpath)??>${content.rootpath}<#else></#if>js/jquery-1.11.2.min.js"></script>
    <script src="//maxcdn.bootstrapcdn.com/bootstrap/3.3.1/js/bootstrap.min.js"></script>
    <script src="<#if (content.rootpath)??>${content.rootpath}<#else></#if>js/behavior.js"></script>
    <script src="<#if (content.rootpath)??>${content.rootpath}<#else></#if>js/prettify.js"></script>
    
<#include "analytics.ftl">

  </body>
</html>
