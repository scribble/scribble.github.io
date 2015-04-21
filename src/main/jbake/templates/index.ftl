<#include "header.ftl">
<#include "navigation.ftl">

<div class="homepage">
    <section class="main-banner">
        <div class="container">
          <h1 class="fade-in-up one">
            Scribble: Describing Multi Party Protocols
          </h1>
        </div>
      </section>

    <section class="description">
<p>
Scribble is a language to describe application-level protocols among communicating systems. A protocol represents an agreement on how participating systems interact with each other. Without a protocol, it is hard to do meaningful interaction: participants simply cannot communicate effectively, since they do not know when to expect the other parties to send data, or whether the other party is ready to receive data.

However, having a description of a protocol has further benefits. It enables verification to ensure that the protocol can be implemented without resulting in unintended consequences, such as deadlocks.
</p>
    </section>

    <section class="main-features">
        <div class="container">
            <div class="row">
            <div class="col-lg-2">
              <h3>Describe <span class="fa fa-pencil"></span></h3>
              <p>Scribble is a language for describing multiparty protocols from a global, or endpoint neutral, perspective.</p>
            </div>
            <div class="col-lg-3">
              <h3>Verify <span class="fa fa-thumbs-up"></span></h3>
              <p>Scribble has a theoretical foundation, based on the Pi Calculus and Session Types, to ensure that protocols
		described using the language are sound, and do not suffer from deadlocks or livelocks.</p>
           </div>
            <div class="col-lg-2">
              <h3>Project <span class="fa fa-arrows-alt"></h3>
              <p>Endpoint projection is the term used for identifying the responsibility of a particular role (or endpoint)
		within a protocol.</p>
            </div>
            <div class="col-lg-3">
              <h3>Implement <span class="fa fa-tasks"></h3>
              <p>Various options exist, including (a) using the endpoint projection for a role to generate a skeleton code, (b) using session type APIs to clearly describe the behaviour, and (c) statically verify the code against the projection.</p>
            </div>
            <div class="col-lg-2">
              <h3>Monitor <span class="fa fa-search"></h3>
              <p>Use the endpoint projection for roles defined within a Scribble protocol, to monitor the activity of
		a particular endpoint, to ensure it correctly implements the expected behaviour.</p>
            </div>

            </div>

	  <hr>

          <h5><i>
"Scribbling is necessary for architects, either physical or computing, since all great ideas of architectural construction come from that unconscious moment, when you do not realise what it is, when there is no concrete shape, only a whisper which is not a whisper, an image which is not an image, somehow it starts to urge you in your mind, in so small a voice but how persistent it is, at that point you start scribbling." (Dr. Kohei Honda, 2007)
          </i></h5>

        </div>
    </section>
</div>

<#include "footer.ftl">
