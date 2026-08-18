# Dispatch Rider Management Platform, Ikorodu Garage Pilot

A last mile delivery system for social commerce vendors in dense Nigerian markets, built around bicycle riders and orders that can have more than one delivery address.

This is a Spring Boot backend for the MVP described in the project's Business Requirements Document. I'm writing this README more like a short project writeup than a plain setup guide, since it doubles as documentation for a scholarship application.

## What problem this solves

Vendors who sell through WhatsApp and Instagram in places like Ikorodu Garage usually send deliveries by hiring a dispatch rider per package. If a vendor has five customers to deliver to in one day, that means five separate riders paid in full, even if all five addresses are close together.

Existing delivery apps like Gokada, Kwik, Fez, and GIG are built for motorcycles, single recipients, and citywide delivery. None of them let a vendor create one order with several stops and have it treated as one trip.

## What the MVP actually does

* A sender can create one order with one pickup point and several drop off stops
* Riders only use bicycles. Motorcycles were left out on purpose because fuel cost does not work with the pricing this pilot uses
* Rider assignment is manual for now. An admin looks at pending orders and assigns a rider by hand instead of the system picking automatically
* Each stop is priced on its own, based on distance from the previous stop in the route, not straight line distance from the pickup point
* Delivery is confirmed with a one time code per stop, shown to the sender after payment, which they forward to whoever is receiving that stop
* Coverage is limited to one hub for now, about five kilometers around Ikorodu Garage

Things like automatic rider matching, real route optimization, live GPS tracking, and multiple hubs are left out on purpose. The idea is to get a working core flow first and use real pilot numbers to decide whether the automation is even worth building.

## A few design decisions worth explaining

**Pricing is based on the leg of the trip, not the pickup point.** If every stop were priced by straight line distance from pickup, a vendor with three stops clustered near each other but far from the hub would get overcharged. Pricing each stop from the point before it in the route is fairer and matches how the rider actually travels.

**Stop order is decided by nearest neighbor, not a full route optimization.** The sender can enter stops in any order. The system just picks whichever unvisited stop is closest each time. It's not the mathematically optimal route, but it's good enough at the number of stops a bicycle trip will realistically carry, and the admin can still review and change the order before assigning a rider.

**There's a real race condition in payment and assignment, and it's handled on purpose.** Before a sender can pay, the system checks that at least one rider is active. But two orders could pass that check at almost the same time before either one actually gets a rider. So there's a scheduled job that checks every minute for orders that have been paid for too long without a rider assigned, and it refunds and cancels those automatically instead of letting them sit there.

**The delivery code does not need SMS.** Each stop gets its own one time code once payment goes through, and all the codes are shown to the sender at once in the app. The sender is already logged in, so the code only needs to reach them, not the recipient directly. They pass it along however they already talk to their customers, WhatsApp or a phone call.

## What's built, mapped to the requirements doc

* Registration and login for business owners, riders, and admin, all JWT based
* Order creation with multiple stops, priced and sequenced automatically
* Payment through Paystack, with the webhook confirming payment rather than trusting the client
* The race condition scheduler described above
* Manual admin assignment and a rider status system (active, busy, offline)
* Per stop status updates and the one time code proof of delivery
* Basic pilot metrics for admin, vendors onboarded, orders completed, repeat usage, average stops per order
* Hub is its own entity with a center point and radius, so adding a second hub later is a data change, not a rebuild

Rider payout is deliberately not automated. That part stays manual and ledger based for the pilot, which is what the requirements doc calls for.

## Running it

```
mvn spring-boot:run
```

Needs Java 17 or newer and Maven. On first boot it seeds a default admin account (phone 00000000000, password changeme) and the Ikorodu Garage hub at a five kilometer radius.

Set these as environment variables before running this anywhere beyond your own machine:

| Variable | What it's for |
|---|---|
| JWT_SECRET | replaces the dev default in application.yml |
| PAYSTACK_SECRET_KEY | needed for payment start and webhook confirmation |
| PAYSTACK_CALLBACK_URL | where Paystack sends the user after checkout |
| DISTANCE_MATRIX_USE_MOCK | true uses a free distance estimate with no API key, false needs a real Google key |
| GOOGLE_DISTANCE_MATRIX_API_KEY | only needed once the mock is turned off |

## API endpoints

```
POST   /api/auth/register/business-owner   (public)
POST   /api/auth/register/rider            (public)
POST   /api/auth/login                     (public)

POST   /api/orders                         (business owner)
GET    /api/orders                         (business owner)
GET    /api/orders/{id}                    (business owner)
POST   /api/orders/{id}/pay                (business owner)
POST   /api/orders/{id}/cancel             (business owner)

PATCH  /api/riders/me/status               (rider)
GET    /api/riders/me/orders               (rider)
PATCH  /api/riders/me/stops/{stopId}       (rider)

GET    /api/admin/orders/pending           (admin)
GET    /api/admin/riders/available         (admin)
POST   /api/admin/orders/{id}/assign       (admin)
GET    /api/admin/riders/pending-approval  (admin)
POST   /api/admin/riders/{id}/approve      (admin)
POST   /api/admin/riders/{id}/reject       (admin)
GET    /api/admin/metrics                  (admin)

POST   /api/payments/webhook               (public, Paystack signature checked)
```

## What's not done yet, honestly

* No file upload for rider ID verification. Right now it just expects a URL, actual upload handling to something like S3 wasn't part of the requirements doc
* The payment webhook looks up the matching order by scanning all orders instead of an indexed query. Fine at pilot volume, should be fixed before this grows
* No automated tests. If I had more time this is the first thing I'd add, especially for the pricing logic, the stop sequencing, and the refund scheduler, since those carry the most business logic
* Login is just phone number and password, no refresh tokens, no OTP login
* This code has been checked carefully by hand against the requirements doc but hasn't been compiled and run end to end yet. Running mvn compile locally should be the first step before treating it as finished

## What the pilot is supposed to measure

Once this is actually running with real vendors, the numbers that matter are how many vendors sign up, how many orders get completed, how many stops the average order has (this is really the test of whether the multi address idea works), how often a vendor comes back and places a second order, what a delivery actually costs versus what's charged, and how much a rider earns per active day. None of the current pricing or radius numbers are final, they're pilot assumptions meant to be corrected once real data comes in.

## Where this goes next

1. Finish the MVP build, which is this repo
2. Run the actual pilot in Ikorodu Garage, onboard real vendors and riders
3. Use pilot numbers, not projected automation, to make the case to investors
4. Once funded, build the automation, real route optimization, live tracking, algorithmic matching
5. Expand to more hubs and add motorcycle riders for longer trips outside the bicycle range